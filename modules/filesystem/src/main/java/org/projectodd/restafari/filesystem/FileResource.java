package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryContentSink;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.AsyncFile;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class FileResource implements FSResource, BinaryResource {

    public FileResource(FSResource parent, File file) {
        this.parent = parent;
        this.file = file;
    }

    @Override
    public Vertx vertx() {
        return this.parent.vertx();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String mimeType() {
        return "text/plain";
    }

    @Override
    public String id() {
        return this.file.getName();
    }

    @Override
    public void read(String id, Responder responder) {
        responder.readNotSupported( this );
    }

    @Override
    public void readContent(BinaryContentSink sink) {
        vertx().fileSystem().open(file.getPath(), (result) -> {
            if (result.succeeded()) {
                AsyncFile asyncFile = result.result();
                asyncFile.dataHandler((buffer) -> {
                    sink.accept(buffer.getByteBuf());
                });
                asyncFile.endHandler((end) -> {
                    try {
                        sink.close();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                });
            } else {
                try {
                    sink.close();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }


    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    public String toString() {
        return "[FileResource: file=" + this.file + "]";
    }

    private FSResource parent;
    private File file;
}
