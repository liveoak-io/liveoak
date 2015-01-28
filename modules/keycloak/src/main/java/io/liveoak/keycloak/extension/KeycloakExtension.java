package io.liveoak.keycloak.extension;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.KeycloakServices;
import io.liveoak.keycloak.interceptor.AuthInterceptor;
import io.liveoak.keycloak.interceptor.AuthInterceptorService;
import io.liveoak.keycloak.service.KeycloakConfigResourceService;
import io.liveoak.keycloak.service.KeycloakConfigService;
import io.liveoak.keycloak.service.KeycloakResourceService;
import io.liveoak.keycloak.client.SecurityClientService;
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
 * @author Ken Finnigan
 */
public class KeycloakExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();
        target.addService(KeycloakServices.address(), new KeycloakConfigService())
                .install();

        ServiceName serviceName = Services.systemResource(context.moduleId(), context.id());

        KeycloakConfigResourceService resource = new KeycloakConfigResourceService(context.id());
        target.addService(serviceName, resource)
                .addDependency(KeycloakServices.address(), KeycloakConfig.class, resource.address())
                .install();

        context.mountPrivate(serviceName);

        // Install AuthInterceptor
        AuthInterceptorService authInterceptor = new AuthInterceptorService();
        ServiceController<AuthInterceptor> authController = target.addService(Services.interceptor("auth"), authInterceptor)
                .addDependency(Services.CLIENT, Client.class, authInterceptor.clientInjector())
                .install();
        InterceptorRegistrationHelper.installInterceptor(target, authController);

        // Install Security Client
        SecurityClientService securityClientService = new SecurityClientService();
        target.addService(Services.SECURITY_CLIENT, securityClientService)
                .addDependency(KeycloakServices.address(), KeycloakConfig.class, securityClientService.configInjector());
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        KeycloakResourceService resource = new KeycloakResourceService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), resource)
                .addDependency(KeycloakServices.address(), KeycloakConfig.class, resource.address())
                .install();

        context.mountPublic();
        context.mountPrivate(new DefaultRootResource(context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
