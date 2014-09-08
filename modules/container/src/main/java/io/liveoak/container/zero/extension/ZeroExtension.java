package io.liveoak.container.zero.extension;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.ZeroServices;
import io.liveoak.container.zero.service.ZeroResourcesService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class ZeroExtension implements Extension {

    public static String EXTENSION_ID = "admin";
    public static String APPLICATION_ID = "admin";
    public static String APPLICATION_NAME = "LiveOak Administration";

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        ZeroResourcesService appResources = new ZeroResourcesService();

        target.addService(ZeroServices.BOOTSTRAP.append("resources"), appResources)
                .addDependency(Services.application(APPLICATION_ID), InternalApplication.class, appResources.applicationInjector())
                .install();

        // DO NOT mountPrivate, since this isn't exposed as a valid extension.
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
