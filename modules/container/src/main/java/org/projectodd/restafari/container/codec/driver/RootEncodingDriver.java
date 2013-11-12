package org.projectodd.restafari.container.codec.driver;

import org.projectodd.restafari.container.codec.Encoder;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class RootEncodingDriver extends ResourceEncodingDriver {

    public RootEncodingDriver(RequestContext requestContext, Encoder encoder, Resource resource, Runnable completionHandler) {
        super( resource );
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
