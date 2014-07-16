package io.liveoak.container.zero;

import io.liveoak.container.tenancy.SimpleResourceRegistry;

/**
 * @author Bob McWhirter
 */
public class SystemResource extends SimpleResourceRegistry {

    public SystemResource() {
        super("system");
    }

}
