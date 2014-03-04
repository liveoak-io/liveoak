package io.liveoak.mongo.extension;

import io.liveoak.mongo.RootMongoResource;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;

/**
 * @author Bob McWhirter
 */
public class MongoExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate( new DefaultRootResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        RootMongoResource publicResource = new RootMongoResource( context.resourceId() );
        RootMongoConfigResource privateResource = publicResource.configuration();

        context.mountPublic( publicResource );
        context.mountPrivate( privateResource );
    }


    public void unextend(ApplicationExtensionContext context) throws Exception {

    }

}
