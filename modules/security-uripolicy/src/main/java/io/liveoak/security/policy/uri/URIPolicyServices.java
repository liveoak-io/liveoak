package io.liveoak.security.policy.uri;

import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyServices {

    private static final ServiceName URI_POLICY = Services.LIVEOAK.append("uri-policy");

    public static ServiceName policy(String appId, String resourceId) {
        return URI_POLICY.append("policy", appId, resourceId);
    }

}
