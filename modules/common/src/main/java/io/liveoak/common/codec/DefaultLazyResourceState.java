/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.LazyResourceState;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultLazyResourceState implements LazyResourceState {

    private final MediaType mediaType;
    private ByteBuf buffer;
    private ResourceCodecManager mgr;
    private ResourceState delegate;
    private FileUpload fileUpload;
    private boolean delegateFromFileUpload;

    public DefaultLazyResourceState(ResourceCodecManager mgr, MediaType mediaType) {
        this.mgr = mgr;
        this.mediaType = mediaType;
    }

    public DefaultLazyResourceState(ResourceCodecManager mgr, MediaType mediaType, ByteBuf content) {
        this.mgr = mgr;
        this.mediaType = mediaType;
        this.buffer = content;
    }

    private synchronized ResourceState delegate() {
        try {
            // delegate may exist already - created in-memory before fileUpload was set
            if (!delegateFromFileUpload) {
                // if fileUpload is suddenly non-null we have to switch to using it
                if (fileUpload != null) {
                    if (!fileUpload.isInMemory()) {
                        throw new RuntimeException("Received body is too big for memory!");
                    }
                    buffer = fileUpload.content();
                    delegateFromFileUpload = true;
                    ResourceState old = delegate;
                    delegate = mgr.decode(mediaType, buffer);
                    // copy id, and properties from old over new.
                    // without rewriting id it could be null - fatal.
                    // (UpdateStep.createResponder()#noSuchResource() relies on this as well)
                    if (old != null) {
                        delegate.id(old.id());
                        for (String name: old.getPropertyNames()) {
                            delegate.putProperty(name, old.getProperty(name));
                        }
                    }
                }
            }

            // this takes care of cases when fileUpload is null
            if (delegate == null) {
                delegate = mgr.decode(mediaType, buffer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode message: " + (buffer == null ? null : buffer.toString(Charset.forName("utf-8"))), e);
        }
        return delegate;
    }

    @Override
    public String id() {
        return delegate().id();
    }

    @Override
    public void id(String id) {
        delegate().id(id);
    }

    @Override
    public void uri(URI uri) {
        delegate().uri(uri);
    }

    @Override
    public URI uri() {
        return delegate().uri();
    }

    @Override
    public void putProperty(String name, Object value) {
        delegate().putProperty(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return delegate().getProperty(name);
    }

    @Override
    public Object removeProperty(String name) {
        return delegate().removeProperty(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate().getPropertyNames();
    }

    @Override
    public void addMember(ResourceState member) {
        delegate().addMember(member);
    }

    @Override
    public List<ResourceState> members() {
        return delegate().members();
    }

    /**
     * Get content length.
     *
     * @return Length of posted / uploaded content.
     */
    @Override
    public long getContentLength() {
        if (fileUpload != null) {
            return fileUpload.length();
        }
        if (buffer != null) {
            return buffer.readableBytes();
        }
        return 0;
    }

    /**
     * Find out if body content fit into memory or if it was cached to file on disk.
     *
     * @return true if content was written to disk, false if it fit into memory
     */
    @Override
    public boolean hasBigContent() {
        return fileUpload != null && !fileUpload.isInMemory();
    }

    /**
     * Get direct File reference to entire body content cached in a file if one exists.
     *
     * @return Temp File with body content or null if content is small enough to fit in memory
     */
    @Override
    public File contentAsFile() {
        try {
            return fileUpload != null ? fileUpload.getFile() : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve fileUpload file: " + fileUpload, e);
        }
    }

    /**
     * Get content as InputStream.
     *
     * @return InputStream
     */
    @Override
    public InputStream contentAsStream() {
        if (fileUpload != null) {
            return new FileUploadInputStream(fileUpload);
        }
        if (buffer != null) {
            return new ByteBufInputStream(buffer);
        }
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    /**
     * Get content as ByteBuf.
     *
     * Make sure to call hasBigContent() beforehand, and only invoke this method if it returns false,
     * as otherwise there is a high risk of OOME.
     *
     * @return in-memory ByteBuf containing entire body
     */
    @Override
    public ByteBuf contentAsByteBuf() {
        if (fileUpload != null) {
            return fileUpload.content();
        }
        if (buffer != null) {
            return buffer.copy(0, buffer.readableBytes());
        }
        return Unpooled.buffer();
    }

    @Override
    public void fileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    @Override
    public void content(ByteBuf content) {
        this.buffer = content;
    }
}
