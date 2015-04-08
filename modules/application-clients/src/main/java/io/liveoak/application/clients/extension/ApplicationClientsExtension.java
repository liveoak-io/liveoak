package io.liveoak.application.clients.extension;

import io.liveoak.application.clients.service.ApplicationClientsResourceService;
import io.liveoak.security.client.DirectAccessClient;
import io.liveoak.security.client.SecurityClient;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ApplicationClientsResourceService service = new ApplicationClientsResourceService(context.application());
        ServiceName serviceName = Services.adminResource(context.application().id(), context.resourceId());
        context.target().addService(serviceName, service)
                .addDependency(Services.SECURITY_CLIENT, SecurityClient.class, service.securityClientInjector())
                .addDependency(Services.SECURITY_DIRECT_ACCESS_CLIENT, DirectAccessClient.class, service.directAccessClientInjector())
                .install();

        context.mountPrivate(serviceName);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
