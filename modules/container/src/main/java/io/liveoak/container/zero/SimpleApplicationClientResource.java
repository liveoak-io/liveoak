package io.liveoak.container.zero;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class SimpleApplicationClientResource implements SynchronousResource {

    public SimpleApplicationClientResource(ApplicationClientsResource parent, ResourceState state) {
        this.parent = parent;
        this.id = (String) state.getProperty(LiveOak.ID);
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
    public ResourceState properties(RequestContext ctx) throws Exception {
        ResourceState result = new DefaultResourceState();
        result.putProperty("type", this.type);
        result.putProperty("security-key", this.securityKey);
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.type = (String) state.getProperty("type");
        this.securityKey = (String) state.getProperty("security-key");
        this.parent.updateConfig();
        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.delete(this);
        responder.resourceDeleted(this);
    }

    public ObjectNode toJson() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put(LiveOak.ID, this.id);
        json.put("type", this.type);
        json.put("security-key", this.securityKey);
        return json;
    }

    private ApplicationClientsResource parent;

    private String id;
    private String type;
    private String securityKey;
}
