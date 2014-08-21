package io.liveoak.container.zero.extension;

import io.liveoak.container.zero.ApplicationClientsResource;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        context.mountPrivate(new ApplicationClientsResource());
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
