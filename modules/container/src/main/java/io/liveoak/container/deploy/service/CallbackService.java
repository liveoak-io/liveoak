package io.liveoak.container.deploy.service;

import java.util.function.Consumer;

import io.liveoak.spi.container.Deployer;
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
public class CallbackService implements Service<Void> {

    public CallbackService(Consumer<Deployer.DeploymentResult> callback) {
        this.callback = callback;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.callback.accept( new Deployer.DeploymentResult( this.resourceInjector.getValue() ) );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();

    private Consumer<Deployer.DeploymentResult> callback;
}
