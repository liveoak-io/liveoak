package io.liveoak.keycloak;

import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakAdmin {

    private KeycloakConfig config;
    private String token;

    public KeycloakAdmin(KeycloakConfig config, String token) {
        this.config = config;
        this.token = token;
    }

    public List<RealmRepresentation> getRealms() {
        return null;
    }

    public RealmRepresentation getRealm(String name) {
        return null;
    }

    public List<ApplicationRepresentation> getApplications(String realm) {
        return null;
    }

    public ApplicationRepresentation getApplication(String realm, String name) {
        return null;
    }

    public void close() {
    }

}
