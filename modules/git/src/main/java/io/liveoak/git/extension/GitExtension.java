package io.liveoak.git.extension;

import io.liveoak.git.GitRepoAdminResource;
import io.liveoak.git.service.GitRepoResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class GitExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        ServiceTarget target = context.target();
        String appId = context.application().id();

        File dir = new File( context.application().directory(), context.resourceId() );
        GitRepoAdminResource privateResource = new GitRepoAdminResource( context.resourceId(), dir );
        context.mountPrivate( privateResource );

        GitRepoResourceService publicResource = new GitRepoResourceService( privateResource, context.resourceId() );

        target.addService(LiveOak.resource(appId, context.resourceId()), publicResource)
                .addDependency(LiveOak.VERTX, Vertx.class, publicResource.vertxInjector())
                .install();

        context.mountPublic();
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
