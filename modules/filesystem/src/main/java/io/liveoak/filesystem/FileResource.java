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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.AsyncFile;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class FileResource implements FSResource, BinaryResource {

    public FileResource(DirectoryResource parent, File file) {
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

    public File file() {
        return this.file;
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
    public long contentLength() {
        return this.file.length();
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
                    asyncFile.close();
                });
            } else {
                sink.close();
            }
        });
    }

    public String toString() {
        return "[FileResource: file=" + this.file + "]";
    }

    private DirectoryResource parent;
    private File file;
}
