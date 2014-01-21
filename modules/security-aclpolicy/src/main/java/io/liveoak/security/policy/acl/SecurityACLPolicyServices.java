package io.liveoak.security.policy.acl;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SecurityACLPolicyServices {
    private static final ServiceName ACL_POLICY = LiveOak.LIVEOAK.append( "acl-policy" );

    public static ServiceName policy(String orgId, String appId ) {
        return ACL_POLICY.append( orgId, appId, "policy" );
    }

    public static ServiceName resource(String orgId, String appId) {
        return ACL_POLICY.append( orgId, appId, "resource" );
    }
}
