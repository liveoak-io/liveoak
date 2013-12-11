package io.liveoak.container.service;

import io.liveoak.container.deploy.DefaultDeployer;
import io.liveoak.container.deploy.DirectDeployer;
import io.liveoak.spi.container.Deployer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class DirectDeployerService implements Service<DirectDeployer> {

    @Override
    public void start(StartContext context) throws StartException {
        this.deployer = new DirectDeployer( context.getController().getServiceContainer() );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public DirectDeployer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.deployer;
    }

    private DirectDeployer deployer;
}
