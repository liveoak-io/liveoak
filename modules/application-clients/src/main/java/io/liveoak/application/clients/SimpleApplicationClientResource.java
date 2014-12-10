package io.liveoak.application.clients;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class SimpleApplicationClientResource implements SynchronousResource, ConfigResource {

    public SimpleApplicationClientResource(ApplicationClientsResource parent, ResourceState state) {
        this.parent = parent;
        this.id = state.id();
        this.type = (String) state.getProperty("type");
        this.securityKey = (String) state.getProperty("security-key");
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("type", this.type);
        result.put("security-key", this.securityKey);
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.type = (String) state.getProperty("type");
        this.securityKey = (String) state.getProperty("security-key");
        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.delete(this);
        responder.resourceDeleted(this);
    }

    private ApplicationClientsResource parent;

    private String id;
    private String type;
    private String securityKey;
}
