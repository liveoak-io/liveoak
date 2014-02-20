package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakSystemResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class KeycloakSystemResourceService implements Service<KeycloakSystemResource> {

    public KeycloakSystemResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new KeycloakSystemResource( context.getChildTarget(), this.id );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public KeycloakSystemResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    private String id;
    private KeycloakSystemResource resource;
}
