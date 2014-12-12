package io.liveoak.filesystem.extension;

import java.io.File;

import io.liveoak.filesystem.FileSystemAdminResource;
import io.liveoak.filesystem.service.FileSystemAdminResourceService;
import io.liveoak.filesystem.service.FilesystemResourceService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceName;
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

        ServiceName privateResourceServiceName = Services.adminResource(context.application().id(), context.resourceId());
        FileSystemAdminResourceService privateResourceService = new FileSystemAdminResourceService(context.resourceId(), initialDir);
        context.target().addService(privateResourceServiceName, privateResourceService)
                .addDependency(Services.VERTX, Vertx.class, privateResourceService.vertxInjector())
                .install();
        context.mountPrivate(privateResourceServiceName);

        FilesystemResourceService publicResource = new FilesystemResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(Services.VERTX, Vertx.class, publicResource.vertxInjector())
                .addDependency(privateResourceServiceName, FileSystemAdminResource.class, publicResource.adminResourceInjector())
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }

}
