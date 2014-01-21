package io.liveoak.git.extension;

import io.liveoak.filesystem.extension.FilesystemExtension;
import io.liveoak.git.GitServices;
import io.liveoak.git.service.GitRepoResourceService;
import io.liveoak.git.service.RepositoryService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.eclipse.jgit.lib.Repository;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

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
        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceName name = GitServices.repository(orgId, appId, context.id());

        RepositoryService repo = new RepositoryService( context.application().directory(), context.id() );
        target.addService(name, repo )
                .install();

        GitRepoResourceService resource = new GitRepoResourceService( context.id() );

        target.addService( name.append( "resource" ), resource )
                .addDependency(LiveOak.VERTX, Vertx.class, resource.vertxInjector())
                .addDependency( name, Repository.class, resource.repositoryInjector() )
                .install();

        context.mountPublic( name.append( "resource" ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
