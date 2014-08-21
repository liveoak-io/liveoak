package io.liveoak.container.zero.extension;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.zero.service.ApplicationClientsResourceService;
import io.liveoak.spi.LiveOak;
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
        ApplicationClientsResourceService service = new ApplicationClientsResourceService();
        ServiceName serviceName = LiveOak.adminResource(context.application().id(), context.resourceId());
        context.target().addService(serviceName, service)
                .addDependency(LiveOak.applicationConfigurationManager(context.application().id()), ApplicationConfigurationManager.class, service.configManagerInjector())
                .install();

        context.mountPrivate(serviceName);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
