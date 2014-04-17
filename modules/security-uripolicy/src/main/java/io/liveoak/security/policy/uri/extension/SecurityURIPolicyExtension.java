package io.liveoak.security.policy.uri.extension;

import io.liveoak.security.policy.uri.SecurityURIPolicyServices;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.policy.uri.service.URIPolicyConfigResourceService;
import io.liveoak.security.policy.uri.service.URIPolicyResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class SecurityURIPolicyExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        URIPolicy policy = new URIPolicy();
        target.addService(SecurityURIPolicyServices.policy(appId, context.resourceId()), new ValueService<>(new ImmediateValue<>(policy)))
                .install();

        URIPolicyResourceService resource = new URIPolicyResourceService(context.resourceId());

        target.addService(LiveOak.resource(appId, context.resourceId()), resource)
                .addDependency(SecurityURIPolicyServices.policy(appId, context.resourceId()), URIPolicy.class, resource.policyInjector())
                .install();

        URIPolicyConfigResourceService configResource = new URIPolicyConfigResourceService(context.resourceId());
        target.addService(LiveOak.adminResource(appId, context.resourceId()), configResource)
                .addDependency(SecurityURIPolicyServices.policy(appId, context.resourceId()), URIPolicy.class, configResource.policyInjector())
                .install();

        context.mountPublic();
        context.mountPrivate();
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
