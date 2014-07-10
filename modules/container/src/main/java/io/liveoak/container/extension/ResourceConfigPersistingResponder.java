package io.liveoak.container.extension;

import java.io.IOException;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class ResourceConfigPersistingResponder extends DelegatingResponder {

    private static final Logger log = Logger.getLogger(ResourceConfigPersistingResponder.class);

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
            log.error("Unable to update resource config for resource id " + this.resource.id(), e);
        }
    }

    private final AdminResourceWrappingResource resource;
    private final ResourceState state;
}
