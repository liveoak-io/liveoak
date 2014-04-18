package io.liveoak.security.policy.drools;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class DroolsPolicyServices {

    private static final ServiceName DROOLS_POLICY = LiveOak.LIVEOAK.append( "drools-policy" );

    public static ServiceName policy(String appId, String resourceId) {
        return DROOLS_POLICY.append( "policy", appId, resourceId );
    }
}
