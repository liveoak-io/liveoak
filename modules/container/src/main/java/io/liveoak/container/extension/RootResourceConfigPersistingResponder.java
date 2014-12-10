package io.liveoak.container.extension;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class RootResourceConfigPersistingResponder extends DelegatingResponder {

    private static final Logger log = Logger.getLogger(RootResourceConfigPersistingResponder.class);

    public RootResourceConfigPersistingResponder(ConfigPersistingWrappingResource resource, ResourceState state, Responder delegate) {
        super(delegate);
        this.resource = resource;
        this.state = state;
    }

    @Override
    public void resourceUpdated(Resource resource) {
        try {
            this.resource.configurationManager().updateResource(this.resource.id(), this.resource.extensionId(), ConversionUtils.convert(this.state));
            super.resourceUpdated(resource);
        } catch (Exception e) {
            log.error("Unable to update resource config for resource id " + this.resource.id(), e);
        }
    }

    private final ConfigPersistingWrappingResource resource;
    private final ResourceState state;
}
