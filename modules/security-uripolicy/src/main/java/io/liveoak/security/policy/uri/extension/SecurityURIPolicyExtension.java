package io.liveoak.security.policy.uri.extension;

import io.liveoak.security.policy.uri.SecurityURIPolicyServices;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.policy.uri.integration.URIPolicyRootResource;
import io.liveoak.security.policy.uri.service.URIPolicyResourceService;
import io.liveoak.security.policy.uri.service.URIPolicyService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
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

        String appId = context.application().id();

        ServiceTarget target = context.target();

        URIPolicyService policy = new URIPolicyService();
        File file = new File(context.application().directory(), "uri-policy-config.json");

        target.addService(SecurityURIPolicyServices.policy(appId, context.resourceId()), policy)
                .addInjection(policy.fileInjector(), file)
                .install();

        URIPolicyResourceService resource = new URIPolicyResourceService(context.resourceId());

        target.addService(LiveOak.resource(appId, context.resourceId()), resource)
                .addDependency(SecurityURIPolicyServices.policy(appId, context.resourceId()), URIPolicy.class, resource.policyInjector())
                .install();

        context.mountPublic();

        context.mountPrivate(new DefaultRootResource(context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
