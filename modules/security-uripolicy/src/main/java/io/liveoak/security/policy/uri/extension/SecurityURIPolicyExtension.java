package io.liveoak.security.policy.uri.extension;

import io.liveoak.security.policy.uri.SecurityURIPolicyServices;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.policy.uri.integration.URIPolicyRootResource;
import io.liveoak.security.policy.uri.service.URIPolicyResourceService;
import io.liveoak.security.policy.uri.service.URIPolicyService;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class SecurityURIPolicyExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();

        URIPolicyService policy = new URIPolicyService();
        File file = new File( context.application().directory(), "uri-policy-config.json" );

        target.addService(SecurityURIPolicyServices.policy( orgId, appId ), policy )
                .addInjection(policy.fileInjector(), file)
                .install();

        URIPolicyResourceService resource = new URIPolicyResourceService( context.id() );

        target.addService( SecurityURIPolicyServices.resource( orgId, appId ), resource )
                .addDependency( SecurityURIPolicyServices.policy( orgId, appId ), URIPolicy.class, resource.policyInjector() )
                .install();

        context.mountPublic( SecurityURIPolicyServices.resource( orgId, appId ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
