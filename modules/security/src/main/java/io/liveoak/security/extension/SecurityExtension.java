package io.liveoak.security.extension;

import io.liveoak.security.SecurityServices;
import io.liveoak.security.service.AuthzPolicyGroupService;
import io.liveoak.security.service.AuthzResourceService;
import io.liveoak.security.spi.AuthzPolicyGroup;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class SecurityExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();


        File authzConfig = new File( context.application().directory(), "authz-config.json" );

        AuthzPolicyGroupService policyGroup = new AuthzPolicyGroupService();

        target.addService( SecurityServices.policyGroup( orgId, appId ), policyGroup )
                .addInjection( policyGroup.fileInjector(), authzConfig )
                .install();

        AuthzResourceService resource = new AuthzResourceService(context.id());

        target.addService(SecurityServices.resource(orgId, appId), resource)
                .addDependency(LiveOak.CLIENT, Client.class, resource.clientInjector() )
                .addDependency(SecurityServices.policyGroup( orgId, appId ), AuthzPolicyGroup.class, resource.policyGroupInjector() )
                .install();

        context.mountPublic( SecurityServices.resource( orgId, appId ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
