package io.liveoak.security.policy.acl.extension;

import io.liveoak.security.policy.acl.AclPolicyConfig;
import io.liveoak.security.policy.acl.AclPolicyRootResource;
import io.liveoak.security.policy.acl.SecurityACLPolicyServices;
import io.liveoak.security.policy.acl.service.AclPolicyConfigService;
import io.liveoak.security.policy.acl.service.AclPolicyRootResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceTarget;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class SecurityACLPolicyExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate( new DefaultRootResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        AclPolicyConfigService policy = new AclPolicyConfigService();
        File file = new File( context.application().directory(), "acl-policy-config.json" );

        target.addService(SecurityACLPolicyServices.policy( appId, context.resourceId() ), policy )
                .addInjection( policy.fileInjector(), file )
                .install();

        AclPolicyRootResourceService resource = new AclPolicyRootResourceService( context.resourceId() );
        target.addService(LiveOak.resource( appId, context.resourceId() ), resource )
                .addDependency(SecurityACLPolicyServices.policy(appId, context.resourceId()), AclPolicyConfig.class, resource.policyInjector())
                .addDependency(LiveOak.CLIENT, Client.class, resource.clientInjector() )
                .install();

        context.mountPublic();

        context.mountPrivate( new DefaultRootResource( context.resourceId() ));

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
