package io.liveoak.keycloak.interceptor;

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
public class AuthInterceptorService implements Service<AuthInterceptor> {

    private InjectedValue<Client> clientInjector = new InjectedValue<>();
    private AuthInterceptor authInterceptor;

    @Override
    public void start(StartContext context) throws StartException {
        authInterceptor = new AuthInterceptor(clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        authInterceptor = null;
    }

    @Override
    public AuthInterceptor getValue() throws IllegalStateException, IllegalArgumentException {
        return authInterceptor;
    }

    public Injector<Client> clientInjector() {
        return clientInjector;
    }
}
