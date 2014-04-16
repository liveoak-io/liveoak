package io.liveoak.security.interceptor;

import io.liveoak.spi.client.Client;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzInterceptorService implements Service<AuthzInterceptor> {

    private InjectedValue<Client> clientInjector = new InjectedValue<>();
    private AuthzInterceptor authzInterceptor;

    @Override
    public void start(StartContext context) throws StartException {
        authzInterceptor = new AuthzInterceptor(clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        authzInterceptor = null;
    }

    @Override
    public AuthzInterceptor getValue() throws IllegalStateException, IllegalArgumentException {
        return authzInterceptor;
    }

    public Injector<Client> clientInjector() {
        return clientInjector;
    }
}
