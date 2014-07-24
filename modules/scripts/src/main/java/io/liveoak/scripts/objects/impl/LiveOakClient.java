package io.liveoak.scripts.objects.impl;

import java.net.URI;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.objects.LocalClient;
import io.liveoak.scripts.objects.RequestContext;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakClient implements LocalClient {

    Client client;

    public LiveOakClient(Client client) {
        this.client = client;
    }

    @Override
    public Resource create(String path, Resource resource) throws Exception {
        ResourceState resourceState = client.create(new io.liveoak.spi.RequestContext.Builder().build(), path, toResourceState(resource));
        return new LiveOakResource(resourceState);
    }

    @Override
    public Resource read(String path) throws Exception {
        ResourceState resourceState = client.read(new io.liveoak.spi.RequestContext.Builder().build(), path);
        return new LiveOakResource(resourceState);
    }

    @Override
    public Resource update(String path, Resource resource) throws Exception {
        ResourceState resourceState = client.update(new io.liveoak.spi.RequestContext.Builder().build(), path, toResourceState(resource));
        return new LiveOakResource(resourceState);
    }

    @Override
    public Resource delete(String path) throws Exception {
        ResourceState resourceState = client.delete(new io.liveoak.spi.RequestContext.Builder().build(), path);
        return new LiveOakResource(resourceState);
    }

    @Override
    public Resource create(String path, Resource resource, RequestContext requestContext) {
        return null;
    }

    @Override
    public Resource read(String path, RequestContext requestContext) {
        return null;
    }

    @Override
    public Resource update(String path, Resource resource, RequestContext requestContext) {
        return null;
    }

    @Override
    public Resource delete(String path, RequestContext requestContext) {
        return null;
    }


    private ResourceState toResourceState(Resource resource) throws Exception {
        ResourceState resourceState = new DefaultResourceState(resource.getID());

        String uri = resource.getURI();
        if (uri != null) {
            resourceState.uri(new URI(resource.getURI()));
        }

        for (Map.Entry<String, Object> entry : resource.getProperties().entrySet()) {
            resourceState.putProperty(entry.getKey(), entry.getValue());
        }

        return resourceState;
    }
}
