package io.liveoak.container.zero;

import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class SystemResource extends SimpleResourceRegistry {

    public SystemResource() {
        super("system");
    }

}
