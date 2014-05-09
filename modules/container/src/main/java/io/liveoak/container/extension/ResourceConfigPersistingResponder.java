package io.liveoak.container.extension;

import java.io.IOException;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceConfigPersistingResponder extends DelegatingResponder {

    public ResourceConfigPersistingResponder(AdminResourceWrappingResource resource, ResourceState state, Responder delegate) {
        super(delegate);
        this.resource = resource;
        this.state = state;
    }

    @Override
    public void resourceUpdated(Resource resource) {
        try {
            this.resource.configurationManager().updateResource(this.resource.id(), this.resource.type(), ConversionUtils.convert(this.state));
            super.resourceUpdated(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final AdminResourceWrappingResource resource;
    private final ResourceState state;
}
