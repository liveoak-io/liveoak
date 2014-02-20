package io.liveoak.filesystem.aggregating.extension;

import io.liveoak.filesystem.FileSystemAdminResource;
import io.liveoak.filesystem.FilesystemServices;
import io.liveoak.filesystem.aggregating.service.AggregatingFilesystemResourceService;
import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.filesystem.service.FilesystemResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

import java.io.File;

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
        context.target().addService(LiveOak.resource(context.application().id(), context.resourceId()), publicResource)
                .addDependency(LiveOak.VERTX, Vertx.class, publicResource.vertxInjector())
                .addInjection(publicResource.adminResourceInjector(), privateResource)
                .install();

        context.mountPublic(LiveOak.resource(context.application().id(), context.resourceId()));
    }
}
