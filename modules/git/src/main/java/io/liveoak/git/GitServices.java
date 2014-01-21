package io.liveoak.git;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class GitServices {

    private static final ServiceName GIT = LiveOak.LIVEOAK.append( "git" );

    public static ServiceName repository(String orgId, String appId, String id) {
        return GIT.append( "repo", orgId, appId, id );
    }
}
