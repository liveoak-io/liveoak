package io.liveoak.container.zero.extension;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.zero.ZeroServices;
import io.liveoak.container.zero.service.LiveOakOrganizationDeployer;
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

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        LiveOakOrganizationDeployer orgDeployer = new LiveOakOrganizationDeployer();

        target.addService(ZeroServices.ZERO.append( "organization" ), orgDeployer )
                .addDependency(LiveOak.ORGANIZATION_REGISTRY, InternalOrganizationRegistry.class, orgDeployer.organizationRegistryInjector() )
                .install();

        ZeroApplicationDeployer appDeployer = new ZeroApplicationDeployer();

        target.addService( ZeroServices.ZERO.append( "application" ), appDeployer )
                .addDependency(LiveOak.organization("liveoak"), InternalOrganization.class, appDeployer.organizationInjector() )
                .install();

        ZeroResourcesService appResources = new ZeroResourcesService();

        target.addService( ZeroServices.ZERO.append( "resources" ), appResources )
                .addDependency( LiveOak.application( "liveoak", "zero" ), InternalApplication.class, appResources.applicationInjector() )
                .install();

        ZeroExtendingService appExtender = new ZeroExtendingService();

        target.addService( ZeroServices.ZERO.append( "extensions" ), appExtender )
                .addDependency( LiveOak.application( "liveoak", "zero" ), InternalApplication.class, appExtender.applicationInjector() )
                .addDependency( LiveOak.extension( "css" ) )
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
