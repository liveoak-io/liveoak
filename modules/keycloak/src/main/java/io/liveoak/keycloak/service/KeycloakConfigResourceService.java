package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.KeycloakConfigRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakConfigResourceService implements Service<KeycloakConfigRootResource> {

    private String id;
    private InjectedValue<KeycloakConfig> address = new InjectedValue<>();
    private KeycloakConfigRootResource resource;

    public KeycloakConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new KeycloakConfigRootResource( this.id, this.address.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public KeycloakConfigRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<KeycloakConfig> address() {
        return this.address;
    }

}
