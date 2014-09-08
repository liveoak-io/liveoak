package io.liveoak.interceptor.service;

import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.container.interceptor.InterceptorManager;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import static io.liveoak.spi.Services.INTERCEPTOR_MANAGER;
import static io.liveoak.spi.Services.interceptor;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorRegistrationHelper {

    public static void installInterceptor(ServiceTarget serviceContainer, String name, Interceptor interceptor) {
        ServiceName serviceName = interceptor(name);

        ServiceController<Interceptor> controller = serviceContainer.addService(serviceName, new ValueService<Interceptor>(new ImmediateValue<Interceptor>(interceptor)))
                .install();

        installInterceptor(serviceContainer, controller);
    }

    public static void installInterceptor(ServiceTarget serviceContainer, ServiceController<? extends Interceptor> interceptor) {
        ServiceName serviceName = interceptor.getName();
        InterceptorRegistrationService registration = new InterceptorRegistrationService(serviceName.getSimpleName());
        serviceContainer.addService(serviceName.append("register"), registration)
                .addDependency(INTERCEPTOR_MANAGER, InterceptorManager.class, registration.interceptorManagerInjector())
                .addDependency(serviceName, Interceptor.class, registration.interceptorInjector())
                .install();
    }
}
