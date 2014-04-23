package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.KeycloakRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class KeycloakResourceService implements Service<KeycloakRootResource> {

    private String id;
    private InjectedValue<KeycloakConfig> address = new InjectedValue<>();
    private KeycloakRootResource resource;

    public KeycloakResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new KeycloakRootResource(this.id, address.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public KeycloakRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<KeycloakConfig> address() {
        return this.address;
    }

}
