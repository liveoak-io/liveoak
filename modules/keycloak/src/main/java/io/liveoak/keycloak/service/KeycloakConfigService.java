package io.liveoak.keycloak.service;

import io.liveoak.keycloak.KeycloakConfig;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakConfigService implements Service<KeycloakConfig> {

    @Override
    public void start(StartContext context) throws StartException {
        this.config = new KeycloakConfig();
    }

    @Override
    public void stop(StopContext context) {
        this.config = null;
    }

    @Override
    public KeycloakConfig getValue() throws IllegalStateException, IllegalArgumentException {
        return this.config;
    }

    private KeycloakConfig config;

}
