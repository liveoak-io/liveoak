package io.liveoak.mongo.internal;

import io.liveoak.mongo.internal.extension.MongoInternalExtension;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalStorageFactory {

    /**
     * Creates an InternalStorage service for a particular resource and returns
     * the ServiceName the resource needs to use for dependency injection.
     *
     * @param serviceContainer
     * @param appName          The name of the application requesting the InternalStorage
     * @param resourceId       The resource id of the resource requesting the InternalStorage
     * @return A ServiceName for the resource to use as a dependency.
     */
    public static ServiceName createService(ServiceTarget serviceContainer, String appName, String resourceId) {

        ServiceName serviceName = MongoInternalExtension.INTERNAL_MONGO_SERVICE_NAME.append(appName).append(resourceId);

        InternalStorageService internalStorageService = new InternalStorageService();
        serviceContainer.addService(serviceName, internalStorageService)
                .addDependency(MongoInternalExtension.INTERNAL_MONGO_SERVICE_NAME, InternalStorageManager.class, internalStorageService.internalStorageManagerInjector)
                .addInjection(internalStorageService.appNameInjector, appName)
                .addInjection(internalStorageService.resourceIdInjector, resourceId)
                .install();

        return serviceName;
    }

    /**
     * Creates an InternalStorage service for a particular resource and returns
     * the ServiceName the resource needs to use for dependency injection.
     *
     * @param context The application extension context
     * @return The ServiceName for the resource to use as a dependency
     */
    public static ServiceName createService(ApplicationExtensionContext context) {
        return createService(context.target(), context.application().id(), context.resourceId());
    }


}
