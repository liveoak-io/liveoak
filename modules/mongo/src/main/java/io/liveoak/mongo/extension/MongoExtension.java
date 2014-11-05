package io.liveoak.mongo.extension;

import io.liveoak.mongo.MongoConfigResourceService;
import io.liveoak.mongo.RootMongoResourceService;
import io.liveoak.mongo.config.MongoDatastoreService;
import io.liveoak.mongo.config.MongoDatastoresRegistry;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class MongoExtension implements Extension {

    public static final ServiceName SYSTEM_MONGO_CONFIG_SERVICE = Services.LIVEOAK.append("system").append("mongo");
    public static final ServiceName SYSTEM_MONGO_ROOT_RESOURCE = Services.LIVEOAK.append("system").append("mongo").append("root");
    public static final ServiceName SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE = Services.LIVEOAK.append("system").append("mongo").append("datastores");

    public static final ServiceName SYSTEM_MONGO_DATASTORE = Services.LIVEOAK.append("system").append("mongo").append("datastore");


    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceName serviceName = Services.systemResource(context.moduleId(), context.id());
        MongoDatastoreService mongoDatastoreService = new MongoDatastoreService();
        context.target().addService(serviceName, mongoDatastoreService)
                .addDependency(SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, MongoDatastoresRegistry.class, mongoDatastoreService.mongoDatastoreInjector)
                .addInjection(mongoDatastoreService.idInjector, context.id())
                .install();
        context.mountPrivate(serviceName);


        MongoDatastoresRegistry mongoDatastoresResource = new MongoDatastoresRegistry();
        ValueService<RootResource> instanceConfigService = new ValueService(new ImmediateValue(mongoDatastoresResource));
        context.target().addService(SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, instanceConfigService).install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        MongoConfigResourceService mongoConfigResourceService = new MongoConfigResourceService(context.resourceId());
        context.target().addService(Services.adminResource(context.application().id(), context.resourceId()), mongoConfigResourceService)
                .addDependency(SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, MongoDatastoresRegistry.class, mongoConfigResourceService.mongoDatastoreInjector)
                .install();

        RootMongoResourceService rootMongoResourceService = new RootMongoResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), rootMongoResourceService)
                .addDependency(Services.adminResource(context.application().id(), context.resourceId()), RootMongoConfigResource.class, rootMongoResourceService.mongoConfigInjector)
                .install();

        context.mountPrivate();
        context.mountPublic();
    }


    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

    public void instance(String id, SystemExtensionContext context) throws Exception {
        ServiceName instanceName = Services.instanceResource(context.moduleId(), id);
        MongoDatastoreService mongoDatastoreService = new MongoDatastoreService();
        context.target().addService(instanceName, mongoDatastoreService)
                .addDependency(SYSTEM_MONGO_DATASTORE_CONFIG_SERVICE, MongoDatastoresRegistry.class, mongoDatastoreService.mongoDatastoreInjector)
                .addInjection(mongoDatastoreService.idInjector, id)
                .install();
        context.mountInstance(instanceName);
    }

}
