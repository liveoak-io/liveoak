package io.liveoak.interceptor.service;

import io.liveoak.spi.container.interceptor.InterceptorManager;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorSystemResourceService implements Service<InterceptorSystemResource> {

    private final String id;
    private InterceptorSystemResource resource;
    private InjectedValue<InterceptorManager> interceptorManagerInjector = new InjectedValue<>();

    public InterceptorSystemResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new InterceptorSystemResource(this.id, interceptorManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public InterceptorSystemResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<InterceptorManager> interceptorManagerInjector() {
        return this.interceptorManagerInjector;
    }
}
