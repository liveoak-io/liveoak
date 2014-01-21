package io.liveoak.container.zero;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class ApplicationsResource extends SimpleResourceRegistry {

    public ApplicationsResource(OrganizationResource parent) {
        super(parent, "applications" );
        this.organization = parent;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File dir = null;
        String dirPath = (String) state.getProperty( "dir" );
        if ( dirPath != null ) {
            dir = new File( dirPath );
        }

        InternalApplication app = this.organization.organization().createApplication(state.id(), (String) state.getProperty("name"), dir);
        responder.resourceCreated( app.resource() );
    }

     private final OrganizationResource organization;

}
