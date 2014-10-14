package io.liveoak.git.extension;

import io.liveoak.git.service.GitRootResourceService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class GitExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        GitRootResourceService gitService = new GitRootResourceService(context.resourceId(), context.application().directory());

        target.addService(Services.adminResource(context.application().id(), context.resourceId()), gitService)
                .install();

        context.mountPrivate();
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
