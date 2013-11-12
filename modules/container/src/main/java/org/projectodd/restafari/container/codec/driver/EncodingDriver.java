package org.projectodd.restafari.container.codec.driver;

import org.projectodd.restafari.container.codec.Encoder;
import org.projectodd.restafari.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface EncodingDriver {

    Encoder encoder();
    Object object();
    RequestContext requestContext();

    void encode() throws Exception;
    void close() throws Exception;

    void encodeNext();

}
