package io.liveoak.keycloak.service;

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
public class KeycloakBaseUrlService implements Service<String> {

    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        return this.config.getValue().getBaseUrl();
    }

    public Injector<KeycloakConfig> configInjector() {
        return this.config;
    }

    private InjectedValue<KeycloakConfig> config = new InjectedValue<>();
}
