package io.liveoak.spi.resource.async;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public interface BinaryResource extends Resource {

    MediaType mediaType();
    void readContent(RequestContext ctx, BinaryContentSink sink);
}
