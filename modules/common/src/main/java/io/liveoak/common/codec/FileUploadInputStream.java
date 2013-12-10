/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class FileUploadInputStream extends BufferedInputStream {

    public FileUploadInputStream(FileUpload fileUpload) {
        this(fileUpload, 8 * 1024);
    }

    public FileUploadInputStream(FileUpload fileUpload, int bufSize) {
        super(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IllegalStateException("Implementation error!");
            }

            public int read(byte [] buf, int pos, int len) throws IOException {
                ByteBuf buffer = fileUpload.getChunk(len);
                if (buffer.readableBytes() == 0) {
                    return -1;
                } else {
                    int cc = len > buffer.readableBytes() ? buffer.readableBytes() : len;
                    buffer.readBytes(buf, pos, cc);
                    return cc;
                }
            }
        }, bufSize);
    }
}
