package org.projectodd.restafari.container.aspects;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public interface ResourceAspect {

    Resource forResource(Resource resource);
}
