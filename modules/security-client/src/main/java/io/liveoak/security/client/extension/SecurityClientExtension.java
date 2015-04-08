package io.liveoak.security.client.extension;

import io.liveoak.security.client.service.DirectAccessClientService;
import io.liveoak.security.client.service.SecurityClientService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Ken Finnigan
 */
public class SecurityClientExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        // Install Security Client
        SecurityClientService securityClientService = new SecurityClientService();
        target.addService(Services.SECURITY_CLIENT, securityClientService)
                .addDependency(Services.SECURITY_BASE_URL, String.class, securityClientService.securityBaseUrlInjector())
                .install();

        // Install Direct Access Client
        DirectAccessClientService directAccessClientService = new DirectAccessClientService();
        target.addService(Services.SECURITY_DIRECT_ACCESS_CLIENT, directAccessClientService)
                .addDependency(Services.SECURITY_BASE_URL, String.class, directAccessClientService.securityBaseUrlInjector())
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
