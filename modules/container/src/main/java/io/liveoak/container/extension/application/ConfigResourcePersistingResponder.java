package io.liveoak.container.extension.application;

import java.io.File;
import java.io.IOException;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author Ken Finnigan
 */
public class ConfigResourcePersistingResponder extends DelegatingResponder {
    public ConfigResourcePersistingResponder(ResourceState state, File configDir, Responder delegate) {
        super(delegate);
        this.state = state;
        this.configurationDirectory = configDir;
    }

    @Override
    public void resourceCreated(Resource resource) {
        ConfigResource configResource = (ConfigResource) resource;
        try {
            configResource.persistConfig(resource, ConversionUtils.convert(this.state), this.configurationDirectory);
        } catch (IOException e) {
            log.error("Unable to update resource config for resource id " + resource.id(), e);
        }
        super.resourceCreated(resource);
    }

    @Override
    public void resourceDeleted(Resource resource) {
        ConfigResource configResource = (ConfigResource) resource;
        try {
            configResource.removeConfig(resource, this.configurationDirectory);
        } catch (IOException e) {
            log.error("Unable to remove resource config for resource id " + resource.id(), e);
        }
        super.resourceDeleted(resource);
    }

    @Override
    public void resourceUpdated(Resource resource) {
        ConfigResource configResource = (ConfigResource) resource;
        try {
            configResource.persistConfig(resource, ConversionUtils.convert(this.state), this.configurationDirectory);
        } catch (IOException e) {
            log.error("Unable to update resource config for resource id " + resource.id(), e);
        }
        super.resourceUpdated(resource);
    }

    private ResourceState state;
    private File configurationDirectory;

    private static final Logger log = Logger.getLogger(ConfigResourcePersistingResponder.class);
}
