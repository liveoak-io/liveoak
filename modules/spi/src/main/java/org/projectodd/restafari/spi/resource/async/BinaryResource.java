package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;

import java.awt.*;

/**
 * @author Bob McWhirter
 */
public interface BinaryResource extends Resource {

    MediaType mediaType();
    void readContent(RequestContext ctx, BinaryContentSink sink);
}
