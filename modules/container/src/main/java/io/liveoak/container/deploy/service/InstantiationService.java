package io.liveoak.container.deploy.service;

import java.util.function.Consumer;

import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.RootResourceFactory;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class InstantiationService implements Service<RootResource> {

    public InstantiationService(RootResourceFactory factory, ResourceState descriptor, Consumer<Deployer.DeploymentResult> callback) {
        this.factory = factory;
        this.descriptor = descriptor;
        this.callback = callback;
    }

    @Override
    public void start(StartContext context) throws StartException {
        String id = context.getController().getName().getSimpleName();
        try {
            this.resource = this.factory.createResource(id, this.descriptor);
        } catch (Exception e) {
            callback.accept( new Deployer.DeploymentResult( e ) );
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    private RootResourceFactory factory;
    private ResourceState descriptor;

    private RootResource resource;

    private Consumer<Deployer.DeploymentResult> callback;
}
