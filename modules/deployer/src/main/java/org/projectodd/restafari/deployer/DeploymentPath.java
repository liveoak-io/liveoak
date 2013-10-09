package org.projectodd.restafari.deployer;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.FileSystems.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Represents a deployment directory for mBaaS applications.
 *
 * @author Lance Ball lball@redhat.com
 */
public class DeploymentPath {

    private final WatchService watchService;
    private final Vertx vertx;
    private Long timerId = -1L;
    private final Path path;

    /**
     * Create a new instance that watches the directory path provided for changes,
     * signalling the DeploymentController to PUT, POST or DELETE a {@linkplain DeploymentResource}
     *
     * @param dir   The directory path on disk where deployments will be placed
     * @param vertx The vertx object
     * @throws IOException if there is an error finding or reading dir
     */
    public DeploymentPath(String dir, Vertx vertx) throws IOException {
        this.vertx = vertx;
        watchService = getDefault().newWatchService();
        path = getDefault().getPath(dir);
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    public void start() {
        vertx.setPeriodic(1000, new Handler<Long>() {
            @Override
            public void handle(Long id) {
                timerId = id;
                WatchKey key = watchService.poll();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        // we don't care about overflow events
                        if (kind == OVERFLOW) { continue; }

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename       = ev.context();
                        Path deployment     = path.resolve(filename);

                        // For now, assume a directory-based deployment style
                        if (!Files.isDirectory(deployment)) {
                            continue;
                        }

                        // outstanding question here - do we handleSend a message on the event bus to create these?
                        if (kind == ENTRY_CREATE) {
                            // create a DeploymentResource
                        } else if (kind == ENTRY_DELETE) {
                            // delete the DeploymentResource
                        } else if (kind == ENTRY_MODIFY) {
                            // update the DeploymentResource
                        }
                    }
                    if (!key.reset()) {
                        vertx.cancelTimer(timerId);
                    }
                }
            }
        });
    }
}
