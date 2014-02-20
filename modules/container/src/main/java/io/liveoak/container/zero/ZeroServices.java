package io.liveoak.container.zero;

import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class ZeroServices {

    public static final ServiceName ZERO = LiveOak.LIVEOAK.append("zero");
    public static final ServiceName BOOTSTRAP = ZERO.append( "bootstrap" );
    public static final ServiceName SYSTEM_ADMIN_MOUNT = ZERO.append( "mount", "system" );

}
