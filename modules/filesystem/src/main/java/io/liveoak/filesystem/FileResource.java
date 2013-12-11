/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.BinaryResourceState;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.AsyncFile;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class FileResource implements FSResource, BinaryResource {

    public FileResource(DirectoryResource parent, File file) {
        this(parent, file, false);
    }

    public FileResource(DirectoryResource parent, File file, boolean writable) {
        this.parent = parent;
        this.file = file;
        this.writable = writable;
    }

    @Override
    public Vertx vertx() {
        return this.parent.vertx();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public File file() {
        return this.file;
    }

    @Override
    public boolean writable() {
        return writable;
    }

    @Override
    public MediaType mediaType() {
        String name = this.file.getName();
        int lastDotLoc = name.lastIndexOf('.');
        MediaType mediaType = null;
        if (lastDotLoc > 0) {
            mediaType = MediaType.lookup(name.substring(lastDotLoc + 1));
        }

        if (mediaType == null) {
            mediaType = MediaType.OCTET_STREAM;
        }
        return mediaType;
    }

    @Override
    public String id() {
        return this.file.getName();
    }

    @Override
    public void readContent(RequestContext ctx, BinaryContentSink sink) {
        vertx().fileSystem().open(file.getPath(), (result) -> {
            if (result.succeeded()) {
                AsyncFile asyncFile = result.result();
                asyncFile.dataHandler((buffer) -> {
                    sink.accept(buffer.getByteBuf());
                });
                asyncFile.endHandler((end) -> {
                    sink.close();
                });
            } else {
                sink.close();
            }
        });
    }

    @Override
    public void updateContent(RequestContext ctx, BinaryResourceState state, Responder responder) {
        boolean append = ctx.resourceParams().booleanValue("append", false);
        vertx().fileSystem().open(file.getPath(), null, false, true, false, true, (openResult) -> {
            if (openResult.succeeded()) {
                final AsyncFile asyncFile = openResult.result();
                //TODO: Need ability to append, I have made change to vertx to expose size so we can write to end of file, need to discuss with tim
                //long position = (append) ? asyncFile.size() : 0;
                int position = 0;
                asyncFile.write(new Buffer(state.getBuffer()), position, writeResult -> {
                    if (writeResult.succeeded()) {
                        responder.resourceUpdated(FileResource.this);
                    } else {
                        responder.internalError("Could not write to file for this resource " + this + ". " + writeResult.cause().getMessage());
                    }
                });
            } else {
                responder.internalError("Trouble opening file for resource. " + openResult.cause().getMessage());
            }
        });
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        if (!writable()) {
            responder.deleteNotSupported(this);
        } else {
            vertx().fileSystem().delete(file.getPath(), result -> {
                if (result.succeeded()) {
                    responder.resourceDeleted(this);
                } else {
                    responder.internalError("Could not delete file for this resource " + this + ". " + result.cause().getMessage());
                }
            });
        }
    }

    public String toString() {
        return "[FileResource: file=" + this.file + "]";
    }

    private DirectoryResource parent;
    private File file;
    private boolean writable;
}
