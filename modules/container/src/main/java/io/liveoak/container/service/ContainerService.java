package io.liveoak.container.service;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.deploy.DirectoryDeploymentManager;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.Deployer;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ContainerService implements Service<Container> {

    public ContainerService() {

    }

    @Override
    public void start(StartContext context) throws StartException {
        this.container = new DefaultContainer();
        this.container.deployer( this.deployerInjector.getValue() );
        this.container.deploymentManager( this.deploymentManagerInjector.getOptionalValue() );
        this.container.start();
    }

    @Override
    public void stop(StopContext context) {
        this.container.shutdown();
    }

    @Override
    public Container getValue() throws IllegalStateException, IllegalArgumentException {
        return this.container;
    }

    public Injector<Deployer> deployerInjector() {
        return this.deployerInjector;
    }

    public Injector<DirectoryDeploymentManager> deploymentManagerInjector() {
        return this.deploymentManagerInjector;
    }

    private DefaultContainer container;
    private InjectedValue<Deployer> deployerInjector = new InjectedValue<>();
    private InjectedValue<DirectoryDeploymentManager> deploymentManagerInjector = new InjectedValue<>();
}
