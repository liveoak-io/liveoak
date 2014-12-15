package io.liveoak.filesystem.aggregating.extension;

import java.io.File;

import io.liveoak.filesystem.FileSystemAdminResource;
import io.liveoak.filesystem.aggregating.service.AggregatingFilesystemResourceService;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.filesystem.service.FileSystemAdminResourceService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class AggregatingFilesystemExtension extends FilesystemExtension {

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        File initialDir = new File(context.application().directory(), context.resourceId());

        ServiceName privateResourceServiceName = Services.adminResource(context.application().id(), context.resourceId());
        FileSystemAdminResourceService privateResourceService = new FileSystemAdminResourceService(context.resourceId(), initialDir);
        context.target().addService(privateResourceServiceName, privateResourceService)
                .addDependency(Services.VERTX, Vertx.class, privateResourceService.vertxInjector())
                .install();
        context.mountPrivate(privateResourceServiceName);

        AggregatingFilesystemResourceService publicResource = new AggregatingFilesystemResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(Services.VERTX, Vertx.class, publicResource.vertxInjector())
                .addDependency(privateResourceServiceName, FileSystemAdminResource.class, publicResource.adminResourceInjector())
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));
    }
}
