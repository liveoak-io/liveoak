package io.liveoak.keycloak;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class KeycloakServices {

    private static final ServiceName KEYCLOAK = LiveOak.LIVEOAK.append( "keycloak" );

    private static final ServiceName UNDERTOW = KEYCLOAK.append( "undertow" );

    public static final ServiceName undertow(String id) {
        return UNDERTOW.append( "server", id );
    }

    public static final ServiceName keycloak(String id) {
        return KEYCLOAK.append( "server", id );
    }

    public static final ServiceName sessionFactory(String id) {
        return keycloak( id ).append( "session-factory" );
    }

    public static final ServiceName realmRepresentation(String appId) {
        return KEYCLOAK.append("realm-representation", appId);
    }

    public static final ServiceName realmModel(String appId) {
        return KEYCLOAK.append("realm-model", appId);
    }

}
