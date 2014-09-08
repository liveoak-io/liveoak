package io.liveoak.mongo.gridfs.extension;

import java.io.File;

import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.mongo.gridfs.service.GridFSResourceService;
import io.liveoak.mongo.gridfs.service.TmpDirService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class GridFSExtension extends MongoExtension {

    public static ServiceName TMP_DIR = Services.LIVEOAK.append("gridfs", "tmpdir");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        // setup Mongo client
        context.mountPrivate(new DefaultRootResource(context.id()));

        ServiceTarget target = context.target();
        target.addService(TMP_DIR, new TmpDirService())
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        RootMongoConfigResource privateResource = new RootMongoConfigResource(context.resourceId());
        context.mountPrivate(privateResource);

        GridFSResourceService publicResource = new GridFSResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(Services.VERTX, Vertx.class, publicResource.vertxInjector())
                .addDependency(TMP_DIR, File.class, publicResource.tmpDirInjector())
                .addInjection(publicResource.configResourceInjector(), privateResource)
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));
    }

}
