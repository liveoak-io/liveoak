package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface EncodingContext<T> {

    T attachment();
    Object object();
    ResourceEncoder<T> encoder();
    void encode() throws Exception;
    void end();

    int depth();

    boolean shouldEncodeContent();
    void encodeContent(Runnable contentCompletionHandler);
    RequestContext requestContext();

    boolean hasAspects();
    void encodeAspects(Runnable aspectCompletionHandler);
}
