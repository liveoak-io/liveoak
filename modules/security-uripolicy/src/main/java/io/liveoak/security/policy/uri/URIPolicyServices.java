package io.liveoak.security.policy.uri;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyServices {

    private static final ServiceName URI_POLICY = LiveOak.LIVEOAK.append("uri-policy");

    public static ServiceName policy(String appId, String resourceId) {
        return URI_POLICY.append("policy", appId, resourceId);
    }

}
