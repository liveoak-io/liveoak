package io.liveoak.filesystem.aggregating.extension;

import io.liveoak.filesystem.FilesystemServices;
import io.liveoak.filesystem.aggregating.service.AggregatingFilesystemResourceService;
import io.liveoak.filesystem.extension.FilesystemExtension;
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
    protected void setUpResource(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();

        ServiceName name = FilesystemServices.directory(orgId, appId, context.id());

        AggregatingFilesystemResourceService resource = new AggregatingFilesystemResourceService( context.id() );

        target.addService(name.append("resource"), resource)
                .addDependency(LiveOak.VERTX, Vertx.class, resource.vertxInjector())
                .addDependency(name, File.class, resource.directoryInjector())
                .install();

        context.mountPublic( name.append( "resource" ) );
    }
}
