package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.keycloak.models.RealmModel;

/**
 * @author Bob McWhirter
 */
public class KeycloakResourceService implements Service<KeycloakRootResource> {

    public KeycloakResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new KeycloakRootResource( this.id, this.realmModelInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public KeycloakRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<RealmModel> realmModelInjector() {
        return this.realmModelInjector;
    }

    private String id;
    private InjectedValue<RealmModel> realmModelInjector = new InjectedValue<>();
    private KeycloakRootResource resource;
}
