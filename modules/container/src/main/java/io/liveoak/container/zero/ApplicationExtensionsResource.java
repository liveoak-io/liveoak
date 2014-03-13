package io.liveoak.container.zero;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.container.util.ConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionsResource extends SimpleResourceRegistry implements BlockingResource {

    public ApplicationExtensionsResource(ApplicationResource parent, String id) {
        super(parent, id);
        this.application = parent;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String extensionId = (String) state.getProperty( "type" );

        //get the 'config' value from the resource state
        ResourceState configState = new DefaultResourceState();
        Object configObject = state.getProperty("config");
        if (configObject != null && configObject instanceof ResourceState) {
            configState = (ResourceState) configObject;
        }

        InternalApplicationExtension ext = this.application.application().extend( extensionId, state.id(), ConversionUtils.convert( configState));
        responder.resourceCreated( ext.adminResource() );
    }

    private final ApplicationResource application;

}
