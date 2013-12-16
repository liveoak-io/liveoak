package io.liveoak.container.service;

import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.spi.container.interceptor.Interceptor;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class InterceptorRegistrationService implements Service<Void> {

    public InterceptorRegistrationService() {

    }

    @Override
    public void start(StartContext context) throws StartException {
        this.interceptorManagerInjector.getValue().register(this.interceptorInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.interceptorManagerInjector.getValue().unregister(this.interceptorInjector.getValue());
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InterceptorManager> interceptorManagerInjector() {
        return this.interceptorManagerInjector;
    }

    public Injector<Interceptor> interceptorInjector() {
        return this.interceptorInjector;
    }


    private InjectedValue<InterceptorManager> interceptorManagerInjector = new InjectedValue<>();
    private InjectedValue<Interceptor> interceptorInjector = new InjectedValue<>();
}
