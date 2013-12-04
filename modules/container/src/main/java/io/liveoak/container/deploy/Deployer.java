package io.liveoak.container.deploy;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface Deployer {
    RootResource deploy(ResourceState state) throws Exception;
}
