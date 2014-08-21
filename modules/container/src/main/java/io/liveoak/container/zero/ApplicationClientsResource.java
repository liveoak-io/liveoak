package io.liveoak.container.zero;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResource implements RootResource, SynchronousResource {
    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "application-clients";
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        SimpleApplicationClientResource client = new SimpleApplicationClientResource(this, state);

        this.applicationClients.put(client.id(), client);
        responder.resourceCreated(client);
    }

    @Override
    public Collection<? extends Resource> members() {
        return this.applicationClients.values();
    }

    @Override
    public Resource member(String id) {
        return this.applicationClients.get(id);
    }

    public void delete(SimpleApplicationClientResource applicationClient) {
        this.applicationClients.remove(applicationClient.id());
    }

    private Resource parent;

    private Map<String,SimpleApplicationClientResource> applicationClients = new ConcurrentHashMap<>();
}
