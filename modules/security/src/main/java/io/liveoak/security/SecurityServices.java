package io.liveoak.security;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SecurityServices {

    private static final ServiceName SECURITY = LiveOak.LIVEOAK.append( "security" );

    public static ServiceName resource(String orgId, String appId) {
        return SECURITY.append( orgId, appId, "resource" );
    }

    public static ServiceName policyGroup(String orgId, String appId) {
        return SECURITY.append( orgId, appId, "policy-group");
    }
}
