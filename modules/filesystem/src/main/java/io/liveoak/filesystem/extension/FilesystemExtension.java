package io.liveoak.filesystem.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.filesystem.FilesystemResource;
import io.liveoak.filesystem.FilesystemServices;
import io.liveoak.filesystem.service.DirectoryService;
import io.liveoak.filesystem.service.FilesystemResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class FilesystemExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        setUpDirectory( context );
        setUpResource( context );
    }

    protected void setUpDirectory(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();
        ServiceName name = FilesystemServices.directory( orgId, appId, context.id() );

        DirectoryService dir = new DirectoryService(new File( context.application().directory(), context.id() ) );

        System.err.println( "setup dir" );
        target.addService(name, dir)
                .addDependency(context.configurationServiceName(), ObjectNode.class, dir.configurationInjector())
                .install();
    }

    protected void setUpResource(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();

        ServiceName name = FilesystemServices.directory( orgId, appId, context.id() );

        FilesystemResourceService resource = new FilesystemResourceService( context.id() );

        target.addService( name.append( "resource" ), resource )
                .addDependency( LiveOak.VERTX, Vertx.class, resource.vertxInjector() )
                .addDependency(name, File.class, resource.directoryInjector())
                .install();

        context.mountPublic( name.append( "resource" ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
     }
}
