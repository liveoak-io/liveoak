package io.liveoak.container.zero;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ApplicationsResource extends SimpleResourceRegistry {

    public ApplicationsResource(InternalApplicationRegistry applicationRegistry) {
        super("applications");
        this.applicationRegistry = applicationRegistry;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File dir = null;
        String dirPath = (String) state.getProperty("dir");
        if (dirPath != null) {
            dir = new File(dirPath);
        }

        InternalApplication app = this.applicationRegistry.createApplication(state.id(), (String) state.getProperty("name"), dir);
        responder.resourceCreated(app.resource());
    }

    private final InternalApplicationRegistry applicationRegistry;


}
