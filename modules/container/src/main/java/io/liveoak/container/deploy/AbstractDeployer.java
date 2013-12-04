package io.liveoak.container.deploy;

import io.liveoak.container.DefaultContainer;
import io.liveoak.spi.Config;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractDeployer implements Deployer {

    public AbstractDeployer(DefaultContainer container) {
        this.container = container;
    }

    protected void register(RootResource resource, Config config) throws InitializationException {
        this.container.registerResource( resource, config );

    }

    private DefaultContainer container;
}
