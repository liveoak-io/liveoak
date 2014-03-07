package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 *
 * Resource to facilitate sending out push notifications to the UPS server.
 *
 * Note: this is done in a send and forget fashion. Messages are not currently saved. The only operation
 * supported is createMember
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSNotifierResource implements Resource {

    public static final String ID = "send";
    UPSRootResource parent;

    public UPSNotifierResource(UPSRootResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        //TODO: implement the ability to send a message to UPS from this resource
        responder.createNotSupported( this );
    }
}
