package io.liveoak.ups;

import io.liveoak.mongo.internal.InternalStorage;
import io.liveoak.mongo.internal.InternalStorageFactory;
import io.liveoak.spi.Services;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.ups.resource.config.UPSRootConfigResource;
import io.liveoak.ups.system.UPSSystemRootResource;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class UPSExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        RootResource systemUpsResource = new UPSSystemRootResource(context.id());

        context.mountPrivate(systemUpsResource);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        // create the configuration resource for the UPSRootResource
        UPSRootConfigResource upsRootConfigResource = new UPSRootConfigResource(context.resourceId());
        // making changes to the configuration is restricted and must be added under private
        context.mountPrivate(upsRootConfigResource);

        ServiceName storageServiceName = InternalStorageFactory.createService(context);

        // setup the main UPSRootResource and add in its dependency on the SubscriptionManager
        UPSResourceService upsResourceService = new UPSResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), upsResourceService)
                .addDependency(Services.SUBSCRIPTION_MANAGER, SubscriptionManager.class, upsResourceService.subscriptionManagerInjector)
                .addDependency(storageServiceName, InternalStorage.class, upsResourceService.internalStorageInjector)
                .addInjection(upsResourceService.configResourceInjector, upsRootConfigResource)
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        //Do nothing for now
    }
}
