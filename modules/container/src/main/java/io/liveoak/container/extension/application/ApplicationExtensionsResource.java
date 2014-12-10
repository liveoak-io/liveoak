package io.liveoak.container.extension.application;

import io.liveoak.common.MediaTypeMountPointResource;
import io.liveoak.container.extension.ConfigVersioningResponder;
import io.liveoak.container.tenancy.ApplicationResource;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.extension.application.InternalApplicationExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ApplicationExtensionsResource extends MediaTypeMountPointResource implements BlockingResource {

    public ApplicationExtensionsResource(ApplicationResource parent, String id, Client client) {
        super(parent, id);
        this.application = parent;
        this.client = client;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        InternalApplication internalApp = this.application.application();
        InternalApplicationExtension ext = internalApp.extend(state.id(), state);
        new ConfigVersioningResponder(responder, internalApp.versioned(), internalApp.versionedResourcePath(), this.client, ctx.securityContext()).resourceCreated(ext.adminResource());
    }

    private final ApplicationResource application;
    private Client client;
}
