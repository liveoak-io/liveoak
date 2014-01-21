package io.liveoak.security.policy.uri;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class SecurityURIPolicyServices {

    private static final ServiceName URI_POLICY = LiveOak.LIVEOAK.append( "uri-policy" );

    public static ServiceName policy(String orgId, String appId) {
        return URI_POLICY.append( orgId, appId, "policy");
    }

    public static ServiceName resource(String orgId, String appId) {
        return URI_POLICY.append( orgId, appId, "resource");
    }
}
