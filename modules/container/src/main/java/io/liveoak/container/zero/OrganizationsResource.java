package io.liveoak.container.zero;

import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class OrganizationsResource extends SimpleResourceRegistry implements BlockingResource {

    public OrganizationsResource(InternalOrganizationRegistry organizationRegistry) {
        super("organizations");
        this.organizationRegistry = organizationRegistry;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        InternalOrganization org = organizationRegistry.createOrganization(state.id(), (String) state.getProperty("name"));
        responder.resourceCreated(org.resource());
    }

    private InternalOrganizationRegistry organizationRegistry;
}
