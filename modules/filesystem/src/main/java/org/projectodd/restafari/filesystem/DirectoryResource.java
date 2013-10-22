package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class DirectoryResource implements FSResource, CollectionResource {

    public DirectoryResource(FSResource parent, File file) {
        this.parent = parent;
        this.file = file;
    }

    public Vertx vertx() {
        return this.parent.vertx();
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        responder.createNotSupported(this);
    }

    @Override
    public void readContent(Pagination pagination, ResourceSink sink) {
        this.parent.vertx().fileSystem().readDir(this.file.getPath(), (result) -> {
            if (result.failed()) {
                try {
                    sink.close();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else {
                for (String filename : result.result()) {
                    File child = new File(filename);
                    if (child.isDirectory()) {
                        sink.accept(new DirectoryResource(this, child));
                    } else {
                        sink.accept(new FileResource(this, child));
                    }
                }
                System.err.println( "dir close: "+ sink );
                try {
                    sink.close();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
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
    public void read(String id, Responder responder) {
        File path = new File(this.file, id);
        this.parent.vertx().fileSystem().exists(path.getPath(), (existResult) -> {
            if (existResult.succeeded() && existResult.result()) {
                if ( path.isDirectory() ) {
                    responder.resourceRead( new DirectoryResource( this, path ) );
                } else {
                    responder.resourceRead( new FileResource( this, path ));
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
        return "[DirectoryResource: file=" + this.file.getAbsolutePath() + "]";
    }

    private FSResource parent;
    private File file;
}
