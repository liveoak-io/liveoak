package io.liveoak.container.resource;

import io.liveoak.container.DefaultContainer;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ContainerConfigurationResource implements Resource {

    public ContainerConfigurationResource(DefaultContainer container) {
        this.container = container;
    }

    @Override
    public Resource parent() {
        return this.container;
    }

    @Override
    public String id() {
        return ";config";
    }

    private DefaultContainer container;
}
