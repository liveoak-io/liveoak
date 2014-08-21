package io.liveoak.container.zero;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResource implements RootResource, SynchronousResource {

    public ApplicationClientsResource(ApplicationConfigurationManager configManager) {
        this.configManager = configManager;
    }

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
        SimpleApplicationClientResource client = createApplicationClient(state);
        updateConfig();
        responder.resourceCreated(client);
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        for (String prop : props.getPropertyNames()) {
            createApplicationClient((ResourceState) props.getProperty(prop));
        }
    }

    @Override
    public Collection<? extends Resource> members() {
        return this.applicationClients.values();
    }

    @Override
    public Resource member(String id) {
        return this.applicationClients.get(id);
    }

    protected SimpleApplicationClientResource createApplicationClient(ResourceState state) {
        SimpleApplicationClientResource client = new SimpleApplicationClientResource(this, state);
        this.applicationClients.put(client.id(), client);
        return client;
    }

    public void updateConfig() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        this.applicationClients.values().stream().forEach((client) -> json.put(client.id(), client.toJson()));
        try {
            this.configManager.updateResource(id(), null, json);
        } catch (IOException e) {
            log.error("Unable to write to 'application.json'.", e);
        }
    }

    public void delete(SimpleApplicationClientResource applicationClient) {
        this.applicationClients.remove(applicationClient.id());
        updateConfig();
    }

    private Resource parent;
    private ApplicationConfigurationManager configManager;
    private Map<String,SimpleApplicationClientResource> applicationClients = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getLogger(ApplicationClientsResource.class);
}
