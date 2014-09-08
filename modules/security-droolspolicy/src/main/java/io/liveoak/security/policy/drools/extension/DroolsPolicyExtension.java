package io.liveoak.security.policy.drools.extension;

import io.liveoak.security.policy.drools.DroolsPolicyServices;
import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.security.policy.drools.service.DroolsPolicyConfigResourceService;
import io.liveoak.security.policy.drools.service.DroolsPolicyResourceService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class DroolsPolicyExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        DroolsPolicy policy = new DroolsPolicy();
        target.addService(DroolsPolicyServices.policy(appId, context.resourceId()), new ValueService<>(new ImmediateValue<>(policy)))
                .install();

        DroolsPolicyResourceService resource = new DroolsPolicyResourceService(context.resourceId());

        target.addService(Services.resource(appId, context.resourceId()), resource)
                .addDependency(DroolsPolicyServices.policy(appId, context.resourceId()), DroolsPolicy.class, resource.policyInjector())
                .install();

        DroolsPolicyConfigResourceService configResource = new DroolsPolicyConfigResourceService(context.resourceId());
        target.addService(Services.adminResource(appId, context.resourceId()), configResource)
                .addDependency(DroolsPolicyServices.policy(appId, context.resourceId()), DroolsPolicy.class, configResource.policyInjector())
                .install();

        context.mountPublic();
        context.mountPrivate();
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
