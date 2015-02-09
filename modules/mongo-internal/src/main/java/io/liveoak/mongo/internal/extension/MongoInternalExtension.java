package io.liveoak.mongo.internal.extension;

import io.liveoak.mongo.MongoConfigResourceService;
import io.liveoak.mongo.config.MongoDatastoresRegistry;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.mongo.internal.InternalMongoService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoInternalExtension implements Extension {

    public static final ServiceName INTERNAL_MONGO_SERVICE_NAME = Services.LIVEOAK.append("internal").append("mongo");
    public static final ServiceName SYSTEM_MONGO_CONFIG_SERVICE = Services.LIVEOAK.append("system").append("mongo");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        log.info("Startup internal Mongo instance");

        InternalMongoService internalMongoService = new InternalMongoService();
        context.target().addService(INTERNAL_MONGO_SERVICE_NAME, internalMongoService)
                .addDependency(SYSTEM_MONGO_CONFIG_SERVICE, RootMongoConfigResource.class, internalMongoService.configResourceInjector)
                .addDependency(SYSTEM_MONGO_CONFIG_SERVICE.append("mount"))
                .install();

        //Create a rootMongoConfigResource here which configures the internal root mongo resource
        MongoConfigResourceService mongoConfigResourceService = new MongoConfigResourceService(context.id());
        context.target().addService(SYSTEM_MONGO_CONFIG_SERVICE, mongoConfigResourceService)
                .addDependency(MongoExtension.SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, MongoDatastoresRegistry.class, mongoConfigResourceService.mongoDatastoreInjector)
                .addDependency(MongoExtension.SYSTEM_MONGO_SERVICE_RESOURCE.append("mount"))
                .install();
        context.mountPrivate(SYSTEM_MONGO_CONFIG_SERVICE);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        //do nothing
    }


    public void unextend(ApplicationExtensionContext context) throws Exception {
        //do nothing
    }

    private static final Logger log = Logger.getLogger(MongoInternalExtension.class);
}
