package io.liveoak.container.deploy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import io.liveoak.container.LiveOak;
import io.liveoak.container.deploy.service.CallbackService;
import io.liveoak.container.deploy.service.ConfigurationService;
import io.liveoak.container.deploy.service.InitializationService;
import io.liveoak.container.deploy.service.RegistrationService;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class DirectDeployer {

    public DirectDeployer(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

    public void deploy(RootResource resource) throws Exception {
        deploy(resource, null);
    }

    public void deploy(RootResource resource, ResourceState config) throws Exception {

        ServiceName name = LiveOak.resource(resource.id());

        CompletableFuture<Void> future = new CompletableFuture<>();
        Consumer<Deployer.DeploymentResult> callback = (result) -> {
            if (result.cause() != null) {
                future.completeExceptionally(result.cause());
            } else {
                future.complete(null);
            }
        };

        serviceContainer.addService(name, new ValueService<RootResource>(new ImmediateValue<RootResource>(resource)))
                .install();

        ConfigurationService configuration = new ConfigurationService(config, callback);
        serviceContainer.addService(name.append("configure"), configuration)
                .addDependency(name, Resource.class, configuration.resourceInjector())
                .install();

        InitializationService initialization = new InitializationService(callback);
        serviceContainer.addService(name.append("initialize"), initialization)
                .addDependency(name, RootResource.class, initialization.resourceInjector())
                .addDependency(LiveOak.CONTAINER, Container.class, initialization.containerInjector())
                .addDependency(LiveOak.NOTIFIER, Notifier.class, initialization.notifierInjector())
                .addDependency(LiveOak.VERTX, Vertx.class, initialization.vertxInjector())
                .addDependency(name.append("configure"))
                .install();

        RegistrationService registration = new RegistrationService();
        serviceContainer.addService(name.append("register"), registration)
                .addDependency(name, RootResource.class, registration.resourceInjector())
                .addDependency(LiveOak.CONTAINER, Container.class, registration.containerInjector())
                .addDependency(name.append("initialize"))
                .install();

        CallbackService callbackSvc = new CallbackService(callback);
        serviceContainer.addService(name.append("callback"), callbackSvc)
                .addDependency(name, RootResource.class, callbackSvc.resourceInjector())
                .addDependency(name.append("register"))
                .install();

        try {
            future.get();
        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private ServiceContainer serviceContainer;

}
