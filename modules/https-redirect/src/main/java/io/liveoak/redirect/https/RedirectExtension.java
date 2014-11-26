package io.liveoak.redirect.https;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.redirect.https.resource.SystemRedirectConfig;
import io.liveoak.spi.Services;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;


/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectExtension implements Extension {

    public static final ServiceName HTTPSREDIRECTOR_SERVICE_NAME = Services.LIVEOAK.append("https-redirect");
    public static final ServiceName HTTPSREDIRECTOR_INTERCEPTOR_SERVICE_NAME = Services.interceptor("https-redirect");
    public static final ServiceName HTTPSREDIRECTOR_MANAGER_SERVICE_NAME = HTTPSREDIRECTOR_SERVICE_NAME.append("manager");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        RedirectInterceptorService service = new RedirectInterceptorService();
        ServiceController controller = target.addService(HTTPSREDIRECTOR_INTERCEPTOR_SERVICE_NAME, service)
                .addDependency(HTTPSREDIRECTOR_MANAGER_SERVICE_NAME, RedirectManager.class, service.redirectManagerInjector)
                .install();

        // Install the interceptor.
        InterceptorRegistrationHelper.installInterceptor(target, controller);

        ServiceName serviceName = Services.systemResource(context.moduleId(), context.id());
        SystemRedirectConfigService configService = new SystemRedirectConfigService(context.id());
        target.addService(serviceName, configService)
                .install();

        context.mountPrivate(serviceName);

        RedirectManagerService redirectManagerService = new RedirectManagerService();
        target.addService(HTTPSREDIRECTOR_MANAGER_SERVICE_NAME, redirectManagerService)
                .addDependency(serviceName, SystemRedirectConfig.class, redirectManagerService.systemRedirectServiceInjector)
                .addDependency(Services.CLIENT, Client.class, redirectManagerService.clientInjector)
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        ServiceName serviceName = Services.resource(context.application().id(), context.resourceId());
        ApplicationRedirectConfigService configService = new ApplicationRedirectConfigService(context.application().id(), context.resourceId());
        context.target().addService(serviceName, configService)
                .addDependency(HTTPSREDIRECTOR_MANAGER_SERVICE_NAME, RedirectManager.class, configService.redirectManagerInjector)
                .install();

        context.mountPrivate(serviceName);
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        // Do nothing for now
    }

}
