package io.liveoak.filesystem.extension;

import java.io.File;

import io.liveoak.filesystem.FileSystemAdminResource;
import io.liveoak.filesystem.service.FilesystemResourceService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class FilesystemExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        File initialDir = new File(context.application().directory(), context.resourceId());
        FileSystemAdminResource privateResource = new FileSystemAdminResource(context.resourceId(), initialDir);

        context.mountPrivate(privateResource);

        FilesystemResourceService publicResource = new FilesystemResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(Services.VERTX, Vertx.class, publicResource.vertxInjector())
                .addInjection(publicResource.adminResourceInjector(), privateResource)
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }

}
