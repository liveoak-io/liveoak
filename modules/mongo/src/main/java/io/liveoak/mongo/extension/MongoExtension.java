package io.liveoak.mongo.extension;

import io.liveoak.mongo.MongoConfigResourceService;
import io.liveoak.mongo.RootMongoResourceService;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.mongo.config.MongoSystemConfigResource;
import io.liveoak.mongo.internal.InternalMongoService;
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

    public static final ServiceName INTERNAL_MONGO_SERVICE_NAME = Services.LIVEOAK.append("internal").append("mongo");
    public static final ServiceName SYSTEM_MONGO_CONFIG_SERVICE = Services.LIVEOAK.append("system").append("mongo");


    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        //Create a rootMongoConfigResource here which configures the internal root mongo resource
        MongoSystemConfigResource
                mongoSystemConfigResource = new MongoSystemConfigResource(context.id());
        ValueService<RootResource> systemConfigService = new ValueService(new ImmediateValue(mongoSystemConfigResource));
        context.target().addService(SYSTEM_MONGO_CONFIG_SERVICE, systemConfigService).install();
        context.mountPrivate(SYSTEM_MONGO_CONFIG_SERVICE);

        InternalMongoService internalMongoService = new InternalMongoService();
        context.target().addService(INTERNAL_MONGO_SERVICE_NAME, internalMongoService)
                .addDependency(SYSTEM_MONGO_CONFIG_SERVICE.append("mount"))
                .addInjection(internalMongoService.configResourceInjector, mongoSystemConfigResource)
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        MongoConfigResourceService mongoConfigResourceService = new MongoConfigResourceService(context.resourceId());
        context.target().addService(Services.adminResource(context.application().id(), context.resourceId()), mongoConfigResourceService)
                .addDependency(SYSTEM_MONGO_CONFIG_SERVICE, MongoSystemConfigResource.class, mongoConfigResourceService.mongoSystemConfigInjector)
                //.addDependency(SYSTEM_MONGO_CONFIG_SERVICE.append("mount"))
                .install();

        RootMongoResourceService rootMongoResourceService = new RootMongoResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), rootMongoResourceService)
                .addDependency(SYSTEM_MONGO_CONFIG_SERVICE, MongoSystemConfigResource.class, rootMongoResourceService.mongoSystemConfigInjector)
                .addDependency(Services.adminResource(context.application().id(), context.resourceId()), RootMongoConfigResource.class, rootMongoResourceService.mongoConfigInjector)
                //.addDependency(Services.adminResource(context.application().id(), context.resourceId()).append("mount"))
                .install();

        context.mountPrivate();
        context.mountPublic();
    }


    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

}
