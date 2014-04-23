package io.liveoak.security.policy.acl.extension;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.SecurityACLPolicyServices;
import io.liveoak.security.policy.acl.interceptor.AclUpdaterInterceptor;
import io.liveoak.security.policy.acl.service.AclPolicyConfigResourceService;
import io.liveoak.security.policy.acl.service.AclPolicyRootResourceService;
import io.liveoak.security.policy.acl.service.AclUpdaterInterceptorService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class SecurityACLPolicyExtension implements Extension {
    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate( new DefaultRootResource( context.id() ));

        // Install AclUpdaterInterceptor
        ServiceTarget target = context.target();
        AclUpdaterInterceptorService aclInterceptor = new AclUpdaterInterceptorService();
        ServiceController<AclUpdaterInterceptor> aclController = target.addService(LiveOak.interceptor("acl-updater"), aclInterceptor)
                .addDependency(LiveOak.CLIENT, Client.class, aclInterceptor.clientInjector())
                .install();
        InterceptorRegistrationHelper.installInterceptor(target, aclController);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        AclPolicy policy = new AclPolicy();
        target.addService(SecurityACLPolicyServices.policy(appId, context.resourceId()), new ValueService<>(new ImmediateValue<>(policy)))
                .install();

        AclPolicyRootResourceService resource = new AclPolicyRootResourceService( context.resourceId() );
        target.addService(LiveOak.resource(appId, context.resourceId()), resource)
                .addDependency(SecurityACLPolicyServices.policy(appId, context.resourceId()), AclPolicy.class, resource.policyInjector())
                .install();

        AclPolicyConfigResourceService configResource = new AclPolicyConfigResourceService( context.resourceId() );
        target.addService(LiveOak.adminResource(appId, context.resourceId()), configResource)
                .addDependency(SecurityACLPolicyServices.policy(appId, context.resourceId()), AclPolicy.class, configResource.policyInjector())
                .install();

        context.mountPublic();
        context.mountPrivate();

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
