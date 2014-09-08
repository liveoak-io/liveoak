package io.liveoak.container.zero;

import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class ZeroServices {

    public static final ServiceName ZERO = Services.LIVEOAK.append("zero");
    public static final ServiceName BOOTSTRAP = ZERO.append("bootstrap");
    public static final ServiceName SYSTEM_ADMIN_MOUNT = ZERO.append("mount", "system");

}
