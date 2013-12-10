package io.liveoak.spi.state;

import java.io.File;
import java.io.InputStream;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface LazyResourceState extends ResourceState {

    public long getContentLength();

    public boolean hasBigContent();

    public File contentAsFile();

    public InputStream contentAsStream();

    public ByteBuf contentAsByteBuf();

    void fileUpload(FileUpload fileUpload);

    public void content(ByteBuf content);
}
