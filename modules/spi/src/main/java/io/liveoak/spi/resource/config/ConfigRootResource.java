package io.liveoak.spi.resource.config;

import java.io.File;
import java.util.List;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * Marker interface for a {@link io.liveoak.spi.resource.RootResource} that will be
 * privately mounted and will have child resources with configuration that needs
 * to be stored in separate files.
 *
 * @author Ken Finnigan
 */
public interface ConfigRootResource extends RootResource {

    /**
     * Defaults to returning "{appDir}/config/{resourceId}.json", but can be overridden
     * to provide a different file name, or a directory into which child resource config
     * files can be added.
     *
     * @param appConfigDir Base dir for all application config.
     * @return Config file as "{resourceId}.json".
     */
    default File baseConfig(File appConfigDir) {
        return new File(appConfigDir, this.id() + ".json");
    }

    @Override
    default void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        List<ResourceState> children = state.members();
        if (children != null) {
            children.forEach(child -> {
                try {
                    this.createMember(ctx, child, new EmptyResponder(responder));
                } catch (Exception e) {
                    log.error("Unable to create child config resource for " + this.getClass().getName(), e);
                }
            });
        }
        responder.resourceUpdated(this);
    }

    static class EmptyResponder extends DelegatingResponder {
        public EmptyResponder(Responder delegate) {
            super(delegate);
        }

        @Override
        public void resourceCreated(Resource resource) {
            // Do nothing
        }
    }

    static final Logger log = Logger.getLogger(ConfigRootResource.class);
}
