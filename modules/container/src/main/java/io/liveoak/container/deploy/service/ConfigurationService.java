package io.liveoak.container.deploy.service;

import java.util.function.Consumer;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.RootResourceFactory;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.platform.impl.Deployment;

/**
 * @author Bob McWhirter
 */
public class ConfigurationService implements Service<Resource> {

    public ConfigurationService(ResourceState config, Consumer<Deployer.DeploymentResult> callback) {
        this.config = config;
        this.callback = callback;
    }

    @Override
    public void start(StartContext context) throws StartException {
        Resource resource = this.resourceInjector.getValue();
        Resource configResource = resource.configuration();

        if (configResource != null && this.config != null) {
            context.asynchronous();
            RequestContext requestContext = new RequestContext.Builder().build();
            try {
                configResource.updateProperties(requestContext, this.config, new ServiceResponder(context));
            } catch (Exception e) {
                this.callback.accept(new Deployer.DeploymentResult(e));
                throw new StartException(e);
            }
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Resource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resourceInjector.getValue();
    }

    public Injector<Resource> resourceInjector() {
        return this.resourceInjector;
    }

    private ResourceState config;
    private Consumer<Deployer.DeploymentResult> callback;

    private InjectedValue<Resource> resourceInjector = new InjectedValue<>();

}
