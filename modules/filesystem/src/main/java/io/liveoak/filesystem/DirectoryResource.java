package io.liveoak.filesystem;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.vertx.java.core.Vertx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class DirectoryResource implements FSResource {

    public DirectoryResource(FSResource parent, File file) {
        this.parent = parent;
        this.file = file;
    }

    public Vertx vertx() {
        return this.parent.vertx();
    }

    public File file() {
        return this.file;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        vertx().fileSystem().readDir(this.file.getPath(), (result) -> {
            if (result.failed()) {
                sink.close();
            } else {
                List<File> sorted = new ArrayList<>();

                for (String filename : result.result()) {
                    File child = new File(filename);
                    sorted.add( child );
                }

                sorted.sort((left, right) -> {
                    if ( left.isDirectory() && right.isDirectory()) {
                        return 0;
                    }

                    if ( left.isFile() && right.isFile() ) {
                        return 0;
                    }

                    if ( left.isDirectory() ) {
                        return -1;
                    }

                    if ( left.isFile() ) {
                        return 1;
                    }

                    return 0;

                });

                for ( File each : sorted ) {
                    if ( each.isDirectory() ) {
                        sink.accept( new DirectoryResource( this, each ));
                    } else {
                        sink.accept( new FileResource( this, each ));
                    }
                }
                sink.close();
            }
        });
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.file.getName();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        File path = new File(this.file, id);
        vertx().fileSystem().exists(path.getPath(), (existResult) -> {
            if (existResult.succeeded() && existResult.result()) {
                if (path.isDirectory()) {
                    responder.resourceRead(new DirectoryResource(this, path));
                } else {
                    responder.resourceRead(new FileResource(this, path));
                }
            } else {
                responder.noSuchResource(id);
            }
        });
    }

    public String toString() {
        return "[DirectoryResource: file=" + this.file.getAbsolutePath() + "]";
    }

    private FSResource parent;
    protected File file;
}
