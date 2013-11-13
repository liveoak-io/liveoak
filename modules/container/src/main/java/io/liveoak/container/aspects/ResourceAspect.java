package io.liveoak.container.aspects;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface ResourceAspect {

    Resource forResource(Resource resource);
}
