/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.driver;

import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class RootEncodingDriver extends ResourceEncodingDriver {

    public RootEncodingDriver( RequestContext requestContext, Encoder encoder, Resource resource, Runnable completionHandler ) {
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
