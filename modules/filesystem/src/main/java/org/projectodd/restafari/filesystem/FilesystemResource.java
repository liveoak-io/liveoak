package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.container.DirectConnector;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class FilesystemResource extends DirectoryResource implements RootResource, FSResource {

    private String id;
    private Vertx vertx;

    public FilesystemResource() {
        super( null, null );
    }

    public FilesystemResource(String id) {
        super( null, null );
        this.id = id;
    }

    @Override
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

        this.file = new File(rootStr);

        if (!this.file.canRead()) {
            throw new InitializationException("unable to read filesystem at: " + this.file.getAbsolutePath());
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

    public String toString() {
        return "[FilesystemResource: root=" + this.file.getAbsolutePath() + "]";
    }
}
