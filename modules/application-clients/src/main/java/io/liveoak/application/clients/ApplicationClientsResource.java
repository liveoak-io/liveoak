package io.liveoak.application.clients;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigRootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResource implements ConfigRootResource, SynchronousResource {

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
        if (this.applicationClients.containsKey(state.id())) {
            responder.resourceAlreadyExists(state.id());
            return;
        }

        SimpleApplicationClientResource client = createApplicationClient(state);
        responder.resourceCreated(client);
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        for (String prop : props.getPropertyNames()) {
            createApplicationClient((ResourceState) props.getProperty(prop));
        }
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) {
        return this.applicationClients.values();
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        return this.applicationClients.get(id);
    }

    protected SimpleApplicationClientResource createApplicationClient(ResourceState state) {
        SimpleApplicationClientResource client = new SimpleApplicationClientResource(this, state);
        this.applicationClients.put(client.id(), client);
        return client;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        if (this.applicationClients != null) {
            this.applicationClients.clear();
        }
        responder.resourceDeleted(this);
    }

    public void delete(SimpleApplicationClientResource applicationClient) {
        this.applicationClients.remove(applicationClient.id());
    }

    private Resource parent;
    private Map<String, SimpleApplicationClientResource> applicationClients = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getLogger(ApplicationClientsResource.class);
}
