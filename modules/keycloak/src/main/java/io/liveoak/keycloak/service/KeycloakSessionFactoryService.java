package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakServer;
import io.liveoak.keycloak.KeycloakSystemResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author Bob McWhirter
 */
public class KeycloakSessionFactoryService implements Service<KeycloakSessionFactory> {

    @Override
    public void start(StartContext context) throws StartException {
        this.sessionFactory = this.keycloakServerInjector.getValue().getKeycloakSessionFactory();
    }

    @Override
    public void stop(StopContext context) {
        this.sessionFactory = null;
    }

    @Override
    public KeycloakSessionFactory getValue() throws IllegalStateException, IllegalArgumentException {
        return this.sessionFactory;
    }

    public Injector<KeycloakServer> keycloakServerInjector() {
        return this.keycloakServerInjector;
    }
    private InjectedValue<KeycloakServer> keycloakServerInjector = new InjectedValue<>();

    private KeycloakSessionFactory sessionFactory;

}
