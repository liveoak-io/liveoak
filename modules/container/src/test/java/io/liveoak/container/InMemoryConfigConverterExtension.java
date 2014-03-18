package io.liveoak.container;

import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.config.WrappingConfigResource;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class InMemoryConfigConverterExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        InMemoryConfigResourceWithConverter configResource = new InMemoryConfigResourceWithConverter(context.resourceId());

        context.mountPrivate(new WrappingConfigResource(context.resourceId(), configResource));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
