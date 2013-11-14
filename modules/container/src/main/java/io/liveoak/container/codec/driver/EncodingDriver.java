package io.liveoak.container.codec.driver;

import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface EncodingDriver {

    Encoder encoder();
    Object object();
    RequestContext requestContext();

    void encode() throws Exception;
    void close() throws Exception;

    EncodingDriver parent();

    void encodeNext();

}
