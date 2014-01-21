package io.liveoak.keycloak;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class KeycloakServices {

    public static final ServiceName KEYCLOAK = LiveOak.LIVEOAK.append( "keycloak" );

    public static final ServiceName UNDERTOW = KEYCLOAK.append( "undertow" );

    public static final ServiceName SESSION_FACTORY = KEYCLOAK.append( "session-factory" );

    public static final ServiceName realmRepresentation(String orgId, String appId) {
        return KEYCLOAK.append( orgId, appId ).append( "realm-representation" );
    }

    public static final ServiceName realmModel(String orgId, String appId) {
        return KEYCLOAK.append( orgId, appId ).append( "realm-model" );
    }

    public static final ServiceName resource(String orgId, String appId) {
        return KEYCLOAK.append( orgId, appId ).append( "resource" );
    }
}
