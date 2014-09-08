package io.liveoak.filesystem.aggregating.extension;

import java.io.File;

import io.liveoak.filesystem.FileSystemAdminResource;
import io.liveoak.filesystem.aggregating.service.AggregatingFilesystemResourceService;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class AggregatingFilesystemExtension extends FilesystemExtension {

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        File initialDir = new File(context.application().directory(), context.resourceId());
        FileSystemAdminResource privateResource = new FileSystemAdminResource(context.resourceId(), initialDir);

        context.mountPrivate(privateResource);

        AggregatingFilesystemResourceService publicResource = new AggregatingFilesystemResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(Services.VERTX, Vertx.class, publicResource.vertxInjector())
                .addInjection(publicResource.adminResourceInjector(), privateResource)
                .install();

        context.mountPublic(Services.resource(context.application().id(), context.resourceId()));
    }
}
