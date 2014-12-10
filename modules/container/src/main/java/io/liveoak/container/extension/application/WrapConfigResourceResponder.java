package io.liveoak.container.extension.application;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Ken Finnigan
 */
public class WrapConfigResourceResponder extends DelegatingResponder {
    public WrapConfigResourceResponder(Responder delegate, InternalApplication application, Client client) {
        super(delegate);
        this.application = application;
        this.client = client;
    }

    @Override
    public void resourceRead(Resource resource) {
        ConfigResourceWrappingResource configResource = new ConfigResourceWrappingResource(resource, application, client);
        super.resourceRead(configResource);
    }

    private InternalApplication application;
    private Client client;
}
