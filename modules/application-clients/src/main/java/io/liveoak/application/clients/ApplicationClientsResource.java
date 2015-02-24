package io.liveoak.application.clients;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.keycloak.client.DirectAccessClient;
import io.liveoak.keycloak.client.SecurityClient;
import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigRootResource;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResource implements ConfigRootResource, SynchronousResource {

    public ApplicationClientsResource(Application application, SecurityClient securityClient, DirectAccessClient directAccessClient) {
        this.application = application;
        this.securityClient = securityClient;
        this.directAccessClient = directAccessClient;
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
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        ConfigRootResource.super.initializeProperties(ctx, state, responder);
        this.directAccessClient.close();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        if (this.applicationClients.containsKey(state.id())) {
            responder.resourceAlreadyExists(state.id());
            return;
        }

        SimpleApplicationClientResource client = createApplicationClient(state, ctx.securityContext());
        responder.resourceCreated(client);
    }

    @Override
    public void properties(RequestContext ctx, ResourceState props) throws Exception {
        for (String prop : props.getPropertyNames()) {
            createApplicationClient((ResourceState) props.getProperty(prop), ctx.securityContext());
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

    protected SimpleApplicationClientResource createApplicationClient(ResourceState state, SecurityContext securityContext) throws Exception {
        SimpleApplicationClientResource client = new SimpleApplicationClientResource(this, this.application, securityContext, state);
        this.applicationClients.put(client.id(), client);
        return client;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        if (this.applicationClients != null) {
            for (SimpleApplicationClientResource resource : this.applicationClients.values()) {
                deleteMember(ctx, resource);
            }
            this.applicationClients.clear();
        }
        responder.resourceDeleted(this);
    }

    public void deleteMember(RequestContext ctx, SimpleApplicationClientResource applicationClient) throws Exception {
        this.applicationClients.remove(applicationClient.id());
        this.securityClient.deleteApplication(ctx.securityContext().getToken(), LiveOak.LIVEOAK_APP_REALM, applicationClient.id());
    }

    public SecurityClient securityClient() {
        return this.securityClient;
    }

    public DirectAccessClient directAccessClient() {
        return this.directAccessClient;
    }

    private Resource parent;
    private SecurityClient securityClient;
    private DirectAccessClient directAccessClient;
    private Application application;
    private Map<String, SimpleApplicationClientResource> applicationClients = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getLogger(ApplicationClientsResource.class);
}
