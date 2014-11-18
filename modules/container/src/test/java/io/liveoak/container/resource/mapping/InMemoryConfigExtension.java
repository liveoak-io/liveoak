package io.liveoak.container.resource.mapping;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;

/**
 * @author Ken Finnigan
 */
public class InMemoryConfigExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        InMemoryConfigResource configResource = new InMemoryConfigResource(context.resourceId());

        context.mountPrivate(configResource);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
