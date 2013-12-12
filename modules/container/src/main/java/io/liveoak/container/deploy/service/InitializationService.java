package io.liveoak.container.deploy.service;

import java.util.function.Consumer;

import io.liveoak.container.DefaultResourceContext;
import io.liveoak.spi.Container;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.DirectConnector;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class InitializationService implements Service<RootResource> {

    public InitializationService(Consumer<Deployer.DeploymentResult> callback) {
        this.callback = callback;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            ResourceContext resourceContext =
                    new DefaultResourceContext(
                            this.containerInjector.getValue(),
                            this.connectorInjector.getValue(),
                            this.vertxInjector.getValue(),
                            this.notifierInjector.getValue());
            this.resourceInjector.getValue().initialize(resourceContext);
        } catch (InitializationException e) {
            e.printStackTrace();
            this.callback.accept(new Deployer.DeploymentResult(e));
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.resourceInjector.getValue().destroy();
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resourceInjector.getValue();
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }

    public Injector<Container> containerInjector() {
        return this.containerInjector;
    }

    public Injector<Notifier> notifierInjector() {
        return this.notifierInjector;
    }

    public Injector<DirectConnector> connectorInjector() {
        return this.connectorInjector;
    }

    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();

    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
    private InjectedValue<Container> containerInjector = new InjectedValue<>();
    private InjectedValue<Notifier> notifierInjector = new InjectedValue<>();
    private InjectedValue<DirectConnector> connectorInjector = new InjectedValue<>();

    private final Consumer<Deployer.DeploymentResult> callback;


}
