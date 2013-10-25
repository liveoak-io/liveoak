package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.ResourceContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

import java.io.File;
import java.util.Arrays;

/**
 * @author Bob McWhirter
 */
public class FilesystemResource implements RootResource, CollectionResource, FSResource {

    private File root;
    private String id;
    private Vertx vertx;

    public FilesystemResource() {
    }

    public FilesystemResource(String id) {
        this.id = id;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {

        if (this.id == null) {
            this.id = context.config().get("id", null);
            if (this.id == null) {
                throw new InitializationException("no id specified");
            }
        }

        String rootStr = context.config().get("root", null);
        if (rootStr == null) {
            throw new InitializationException("no filesystem root specified");
        }

        this.root = new File(rootStr);

        if (!this.root.canRead()) {
            throw new InitializationException("unable to read filesystem at: " + this.root.getAbsolutePath());
        }

        this.vertx = context.vertx();
    }

    @Override
    public void destroy() {
        // nothing.
    }


    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        responder.createNotSupported(this);
    }

    @Override
    public void readContent(Pagination pagination, ResourceSink sink) {
        this.vertx.fileSystem().readDir(this.root.getPath(), (result) -> {
            if (result.failed()) {
                sink.close();
            } else {
                for (String filename : result.result()) {
                    File child = new File(filename);
                    if (child.isDirectory()) {
                        sink.accept(new DirectoryResource(this, child));
                    } else {
                        sink.accept(new FileResource(this, child));
                    }
                }
                sink.close();
            }
        });
    }


    @Override
    public void read(String id, Responder responder) {
        File path = new File(this.root, id);
        this.vertx.fileSystem().exists(path.getPath(), (existResult) -> {
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

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    public String toString() {
        return "[FilesystemResource: root=" + this.root.getAbsolutePath() + "]";
    }
}
