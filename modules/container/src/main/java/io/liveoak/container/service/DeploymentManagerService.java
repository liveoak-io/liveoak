package io.liveoak.container.service;

import java.io.File;

import io.liveoak.container.deploy.DirectoryDeploymentManager;
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
public class DeploymentManagerService implements Service<DirectoryDeploymentManager> {
    @Override
    public void start(StartContext context) throws StartException {
        this.deploymentManager = new DirectoryDeploymentManager(
                this.deployerInjector.getValue(),
                this.directoryInjector.getValue()
        );

        this.deploymentManager.start();
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public DirectoryDeploymentManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.deploymentManager;
    }

    public Injector<Deployer> deployerInjector() {
        return this.deployerInjector;
    }

    public Injector<File> directoryInjector() {
        return this.directoryInjector;
    }

    private InjectedValue<Deployer> deployerInjector = new InjectedValue<>();
    private InjectedValue<File> directoryInjector = new InjectedValue<>();

    private DirectoryDeploymentManager deploymentManager;
}
