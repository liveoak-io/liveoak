package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public interface ExpansionControllingEncoder<T> extends ResourceEncoder<T>  {
    boolean shouldEncodeContent(EncodingContext<T> context);
}
