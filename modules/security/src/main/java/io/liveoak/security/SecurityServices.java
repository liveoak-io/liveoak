package io.liveoak.security;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SecurityServices {

    private static final ServiceName SECURITY = LiveOak.LIVEOAK.append( "security" );

    public static ServiceName policyGroup(String appId) {
        return SECURITY.append( "policy-group", appId);
    }
}
