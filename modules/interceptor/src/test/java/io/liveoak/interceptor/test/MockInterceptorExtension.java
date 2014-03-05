package io.liveoak.interceptor.test;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockInterceptorExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();
        InterceptorRegistrationHelper.installInterceptor(target, "mock-interceptor", new MockCounterInterceptor());
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
