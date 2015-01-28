package io.liveoak.keycloak.client;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.client.SecurityClient;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class SecurityClientService implements Service<SecurityClient> {
    @Override
    public void start(StartContext context) throws StartException {
        client = new SecurityClient(configInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        client = null;
    }

    @Override
    public SecurityClient getValue() throws IllegalStateException, IllegalArgumentException {
        return client;
    }

    public Injector<KeycloakConfig> configInjector() {
        return this.configInjector;
    }

    private SecurityClient client;
    private InjectedValue<KeycloakConfig> configInjector = new InjectedValue<>();
}
