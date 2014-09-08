package io.liveoak.security.policy.acl.extension;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.mongo.internal.InternalStorage;
import io.liveoak.mongo.internal.InternalStorageFactory;
import io.liveoak.security.policy.acl.SecurityACLPolicyServices;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.interceptor.AclUpdaterInterceptor;
import io.liveoak.security.policy.acl.service.AclPolicyConfigResourceService;
import io.liveoak.security.policy.acl.service.AclPolicyRootResourceService;
import io.liveoak.security.policy.acl.service.AclPolicyService;
import io.liveoak.security.policy.acl.service.AclUpdaterInterceptorService;
import io.liveoak.spi.Services;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class SecurityACLPolicyExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        context.mountPrivate(new DefaultRootResource(context.id()));

        // Install AclUpdaterInterceptor
        ServiceTarget target = context.target();
        AclUpdaterInterceptorService aclInterceptor = new AclUpdaterInterceptorService();
        ServiceController<AclUpdaterInterceptor> aclController = target.addService(Services.interceptor("acl-updater"), aclInterceptor)
                .addDependency(Services.CLIENT, Client.class, aclInterceptor.clientInjector())
                .install();
        InterceptorRegistrationHelper.installInterceptor(target, aclController);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        ServiceName mongoStorageServiceName = InternalStorageFactory.createService(context);

        AclPolicyService policy = new AclPolicyService();
        target.addService(SecurityACLPolicyServices.policy(appId, context.resourceId()), policy)
                .addDependency(mongoStorageServiceName, InternalStorage.class, policy.mongoStorageInjector())
                .install();

        AclPolicyRootResourceService resource = new AclPolicyRootResourceService(context.resourceId());
        target.addService(Services.resource(appId, context.resourceId()), resource)
                .addDependency(SecurityACLPolicyServices.policy(appId, context.resourceId()), AclPolicy.class, resource.policyInjector())
                .install();

        AclPolicyConfigResourceService configResource = new AclPolicyConfigResourceService(context.resourceId());
        target.addService(Services.adminResource(appId, context.resourceId()), configResource)
                .addDependency(SecurityACLPolicyServices.policy(appId, context.resourceId()), AclPolicy.class, configResource.policyInjector())
                .install();

        context.mountPublic();
        context.mountPrivate();

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
