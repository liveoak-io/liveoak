package io.liveoak.testtools.resources;

import java.util.UUID;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MockInMemoryRootResource extends MockInMemoryResource implements RootResource {

    public MockInMemoryRootResource(String id) {
        super(id);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState child, Responder responder) throws Exception {
        if (child.id() == null) {
            child.id(UUID.randomUUID().toString());
        }

        super.createMember(ctx, child, responder);
    }
}