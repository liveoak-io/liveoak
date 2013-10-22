package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class RootEncodingContext<T> extends AbstractEncodingContext<T>  {

    public RootEncodingContext(ResourceEncoder<T> encoder, T attachment, Resource resource, Runnable completionHandler) {
        super( null, resource, completionHandler );
        this.attachment = attachment;
        this.encoder = encoder;
    }

    public T attachment() {
        return this.attachment;
    }

    public ResourceEncoder<T> encoder() {
        return this.encoder;
    }

    private ResourceEncoder<T> encoder;
    private T attachment;

}
