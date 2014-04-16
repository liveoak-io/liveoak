package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSMessageResource implements Resource {

    private SubscriptionResource parent;
    public final static String ID = "message";

    Map<String, Object> attributes = new HashMap<String, Object>();


    public UPSMessageResource(SubscriptionResource parent, Map<String, Object> attributes) {
        this.parent = parent;
        this.attributes = attributes;
    }

    //return null since dealing with an embedded resource
    @Override
    public Resource parent() {
        return null;
    }

    //return null since dealing with an embedded resource
    @Override
    public String id() {
        return null;
    }


    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        for (String key : attributes.keySet()) {
            sink.accept( key, attributes.get( key ) );
        }

        sink.close();
    }

    // embedded resource, cannot update the properties here, must do so from the parent resource
    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

}
