package io.liveoak.interceptor.extension;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.interceptor.service.InterceptorSystemResourceService;
import io.liveoak.interceptor.timing.TimingInterceptor;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.container.interceptor.InterceptorManager;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        InterceptorSystemResourceService interceptorSystemResourceService = new InterceptorSystemResourceService(context.id());

        target.addService(LiveOak.systemResource(context.id()), interceptorSystemResourceService)
                .addDependency(LiveOak.INTERCEPTOR_MANAGER, InterceptorManager.class, interceptorSystemResourceService.interceptorManagerInjector())
                .install();

        context.mountPrivate( LiveOak.systemResource( context.id() ));

        InterceptorRegistrationHelper.installInterceptor(target, "timing-http", new TimingInterceptor("HTTP"));
        InterceptorRegistrationHelper.installInterceptor(target, "timing-local", new TimingInterceptor("Local"));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
    }
}
