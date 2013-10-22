package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public interface BinaryResource extends Resource {

    String mimeType();
    void readContent(BinaryContentSink sink);
}
