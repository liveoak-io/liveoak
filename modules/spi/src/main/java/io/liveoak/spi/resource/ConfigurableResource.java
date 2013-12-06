package io.liveoak.spi.resource;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface ConfigurableResource {

    Resource configuration();

}
