package io.liveoak.container.deploy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.liveoak.container.LiveOak;
import io.liveoak.container.deploy.service.CallbackService;
import io.liveoak.container.deploy.service.ConfigurationService;
import io.liveoak.container.deploy.service.InitializationService;
import io.liveoak.container.deploy.service.InstantiationService;
import io.liveoak.container.deploy.service.RegistrationService;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.DirectConnector;
import io.liveoak.spi.container.RootResourceFactory;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class DefaultDeployer implements Deployer {

    public DefaultDeployer(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
    }

    public void registerRootResourceFactory(RootResourceFactory factory) {
        this.factories.put(factory.type(), factory);
    }

    public void unregisterRootResourceFactory(RootResourceFactory factory) {
        this.factories.remove(factory.type());
    }

    @Override
    public void deploy(String id, ResourceState deploymentDescriptor, Consumer<DeploymentResult> callback) throws DeploymentException {
        String type = (String) deploymentDescriptor.getProperty("type");

        RootResourceFactory factory = this.factories.get(type);

        if (factory == null) {
            throw new DeploymentException("Unknown deployment type: " + type);
        }

        ServiceName name = LiveOak.resource(id);

        InstantiationService instantiation = new InstantiationService(factory, deploymentDescriptor, callback);
        serviceContainer.addService(name, instantiation)
                .install();

        ConfigurationService configuration = new ConfigurationService((ResourceState) deploymentDescriptor.getProperty("config"), callback);
        serviceContainer.addService(name.append("configure"), configuration)
                .addDependency(name, Resource.class, configuration.resourceInjector())
                .install();

        InitializationService initialization = new InitializationService(callback);
        serviceContainer.addService(name.append("initialize"), initialization)
                .addDependency(name, RootResource.class, initialization.resourceInjector())
                .addDependency(LiveOak.CONTAINER, Container.class, initialization.containerInjector())
                .addDependency(LiveOak.NOTIFIER, Notifier.class, initialization.notifierInjector())
                .addDependency(LiveOak.VERTX, Vertx.class, initialization.vertxInjector())
                .addDependency(LiveOak.DIRECT_CONNECTOR, DirectConnector.class, initialization.connectorInjector())
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
    }

    private Map<String, RootResourceFactory> factories = new ConcurrentHashMap<>();
    private ServiceContainer serviceContainer;
}
