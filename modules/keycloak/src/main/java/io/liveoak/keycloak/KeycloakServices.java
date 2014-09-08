package io.liveoak.keycloak;

import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class KeycloakServices {

    private static final ServiceName KEYCLOAK = Services.LIVEOAK.append("keycloak");

    private static final ServiceName UNDERTOW = KEYCLOAK.append("undertow");

    public static final ServiceName undertow(String id) {
        return UNDERTOW.append("server", id);
    }

    public static final ServiceName keycloak(String id) {
        return KEYCLOAK.append("server", id);
    }

    public static final ServiceName sessionFactory() {
        return KEYCLOAK.append("session-factory");
    }

    public static final ServiceName address() {
        return KEYCLOAK.append("address");
    }

    public static final ServiceName realmRepresentation(String appId) {
        return KEYCLOAK.append("realm-representation", appId);
    }

    public static final ServiceName realmModel(String appId) {
        return KEYCLOAK.append("realm-model", appId);
    }

}
