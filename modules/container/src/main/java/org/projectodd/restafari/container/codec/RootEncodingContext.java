package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.container.aspects.ResourceAspectManager;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class RootEncodingContext<T> extends AbstractEncodingContext<T>  {

    public RootEncodingContext(RequestContext ctx, ResourceEncoder<T> encoder, T attachment, Resource resource, ResourceAspectManager aspectManager, Runnable completionHandler) {
        super( null, ctx, resource, completionHandler );
        this.attachment = attachment;
        this.encoder = encoder;
        this.aspectManager = aspectManager;
    }

    public T attachment() {
        return this.attachment;
    }

    public ResourceEncoder<T> encoder() {
        return this.encoder;
    }

    public ResourceAspectManager aspectManager() {
        return this.aspectManager;
    }

    public void end() {
        try {
            encoder.close(this);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        super.end();
    }

    private ResourceAspectManager aspectManager;
    private ResourceEncoder<T> encoder;
    private T attachment;

}
