package io.liveoak.spi.container;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface RootResourceFactory {
    String type();

    RootResource createResource(String id, ResourceState descriptor) throws Exception;
}
