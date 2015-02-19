package io.liveoak.keycloak.client;

import io.liveoak.keycloak.KeycloakConfig;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class DirectAccessClientService implements Service<DirectAccessClient> {
    @Override
    public void start(StartContext context) throws StartException {
        client = new DirectAccessClient(configInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        client.shutdown();
        client = null;
    }

    @Override
    public DirectAccessClient getValue() throws IllegalStateException, IllegalArgumentException {
        return client;
    }


    public Injector<KeycloakConfig> configInjector() {
        return this.configInjector;
    }

    private DirectAccessClient client;
    private InjectedValue<KeycloakConfig> configInjector = new InjectedValue<>();
}
