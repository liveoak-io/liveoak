package io.liveoak.container.zero.extension;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.ZeroServices;
import io.liveoak.container.zero.service.ZeroApplicationDeployer;
import io.liveoak.container.zero.service.ZeroExtendingService;
import io.liveoak.container.zero.service.ZeroResourcesService;
import io.liveoak.spi.LiveOak;
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

        ZeroApplicationDeployer appDeployer = new ZeroApplicationDeployer();

        target.addService(ZeroServices.BOOTSTRAP.append("application"), appDeployer)
                .addDependency( LiveOak.APPLICATION_REGISTRY, InternalApplicationRegistry.class, appDeployer.applicationRegistryInjector() )
                .install();

        ZeroResourcesService appResources = new ZeroResourcesService();

        target.addService( ZeroServices.BOOTSTRAP.append( "resources" ), appResources )
                .addDependency(LiveOak.application(APPLICATION_ID), InternalApplication.class, appResources.applicationInjector())
                .install();

        ZeroExtendingService appExtender = new ZeroExtendingService();

        target.addService( ZeroServices.BOOTSTRAP.append( "extensions" ), appExtender )
                .addDependency( LiveOak.application(APPLICATION_ID), InternalApplication.class, appExtender.applicationInjector() )
                .addDependency(LiveOak.extension("filesystem"))
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
