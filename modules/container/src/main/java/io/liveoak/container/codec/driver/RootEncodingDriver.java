package io.liveoak.container.codec.driver;

import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class RootEncodingDriver extends ResourceEncodingDriver {

    public RootEncodingDriver(RequestContext requestContext, Encoder encoder, Resource resource, Runnable completionHandler) {
        super( resource, requestContext.getReturnFields() );
        this.requestContext = requestContext;
        this.encoder = encoder;
        this.completionHandler = completionHandler;
    }

    @Override
    public Encoder encoder() {
        return this.encoder;
    }

    @Override
    public RequestContext requestContext() {
        return this.requestContext;
    }

    @Override
    public void close() throws Exception {
        encoder.close();
        if ( this.completionHandler != null ) {
            this.completionHandler.run();
        }
    }

    private RequestContext requestContext;
    private Encoder encoder;
    private Runnable completionHandler;

}
