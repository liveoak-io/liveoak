package org.projectodd.restafari.container.codec;

/**
 * @author Bob McWhirter
 */
public interface EncodingContext<T> {

    T attachment();
    Object object();
    ResourceEncoder<T> encoder();
    void encode() throws Exception;

    void end();

    boolean shouldEncodeContent();
    void encodeContent(Runnable contentCompletionHandler);
}
