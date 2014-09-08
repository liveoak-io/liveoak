package io.liveoak.mongo.extension;

import io.liveoak.mongo.RootMongoResource;
import io.liveoak.mongo.config.RootMongoConfigResource;
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
    public static final ServiceName INTERNAL_MONGO_CONFIG_SERVICE_NAME = INTERNAL_MONGO_SERVICE_NAME.append("config");


    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        // Create a rootMongoConfigResource here which configures the internal root mongo resource
        RootMongoConfigResource rootMongoConfigResource = new RootMongoConfigResource(context.id());
        ValueService<RootResource> configService = new ValueService(new ImmediateValue(rootMongoConfigResource));
        context.target().addService(INTERNAL_MONGO_CONFIG_SERVICE_NAME, configService).install();
        context.mountPrivate(INTERNAL_MONGO_CONFIG_SERVICE_NAME);

        InternalMongoService internalMongoService = new InternalMongoService();

        context.target().addService(INTERNAL_MONGO_SERVICE_NAME, internalMongoService)
                .addDependency(INTERNAL_MONGO_CONFIG_SERVICE_NAME.append("mount"))
                .addInjection(internalMongoService.configResourceInjector, rootMongoConfigResource)
                .install();

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        RootMongoResource publicResource = new RootMongoResource(context.resourceId());
        RootMongoConfigResource privateResource = publicResource.configuration();

        context.mountPublic(publicResource);
        context.mountPrivate(privateResource);
    }


    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

}
