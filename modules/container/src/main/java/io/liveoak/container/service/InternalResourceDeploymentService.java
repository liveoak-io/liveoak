package io.liveoak.container.service;

import io.liveoak.container.deploy.DirectDeployer;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class InternalResourceDeploymentService implements Service<Void> {

    public InternalResourceDeploymentService() {
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.deployerInjector.getValue().deploy( this.resourceInjector.getValue() );
        } catch (Exception e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        // TODO undeploy!
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    public Injector<DirectDeployer> deployerInjector() {
        return this.deployerInjector;
    }

    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private InjectedValue<DirectDeployer> deployerInjector = new InjectedValue<>();
}
