package io.liveoak.wildfly;

import io.liveoak.container.service.bootstrap.*;
import io.liveoak.mongo.launcher.service.MongoLauncherAutoSetupService;
import io.liveoak.spi.LiveOak;
import org.jboss.as.controller.*;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.registry.ImmutableManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.Services;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import java.net.URL;
import java.util.List;

import static io.liveoak.spi.LiveOak.SERVICE_REGISTRY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author Bob McWhirter
 */
public class LiveOakSubsystemAdd extends AbstractBoottimeAddStepHandler {
    static final LiveOakSubsystemAdd INSTANCE = new LiveOakSubsystemAdd();

    private final Logger log = Logger.getLogger(LiveOakSubsystemAdd.class);

    private LiveOakSubsystemAdd() {
    }

    @Override
    protected void populateModel(OperationContext context, ModelNode operation, Resource resource) throws OperationFailedException {
        System.err.println("OP: " + operation);
        System.err.println("binding: " + operation.get("socket-binding"));
        ModelNode node = new ModelNode();
        node.get("socket-binding").set(operation.get("socket-binding"));
        resource.getModel().set(node);
        System.err.println("resource: " + resource.getModel());
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
                                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                //processorTarget.addDeploymentProcessor(LiveOakExtension.SUBSYSTEM_NAME, SimpleSubsystemDeploymentProcessor.PHASE, SimpleSubsystemDeploymentProcessor.PRIORITY, new SimpleSubsystemDeploymentProcessor());
                processorTarget.addDeploymentProcessor(LiveOakExtension.SUBSYSTEM_NAME, MongoAutoSetupDependencyProcessor.PHASE, MongoAutoSetupDependencyProcessor.PRIORITY, new MongoAutoSetupDependencyProcessor());
            }
        }, OperationContext.Stage.RUNTIME);

        System.err.println("BOOT MODEL: " + model);
        System.err.println("BOOT OP: " + operation);

        ServiceName name = LiveOak.LIVEOAK.append("wildfly", "subsystem");

        ConfDirectoryPathService confDirPath = new ConfDirectoryPathService();
        context.getServiceTarget().addService(name.append("conf-dir", "path"), confDirPath)
                .addDependency(ServiceName.of("jboss", "server", "path", "jboss.home.dir"), String.class, confDirPath.jbossHomeInjector())
                .install();

        MongoLauncherAutoSetupService mongo = new MongoLauncherAutoSetupService();

        context.getServiceTarget().addService(name.append("mongo-autosetup"), mongo)
                .addDependency(name.append("conf-dir", "path"), String.class, mongo.extensionsDirInjector())
                .addDependency(ServiceName.of("jboss", "server", "path", "jboss.home.dir"), String.class, mongo.liveoakDirInjector())
                .install();

        System.err.println("installed mongo auto-setup");

        String socketBinding = model.get("socket-binding").asString();

        LiveOakSocketBindingService liveoakSocketBinding = new LiveOakSocketBindingService();

        context.getServiceTarget().addService(LiveOak.SOCKET_BINDING, liveoakSocketBinding)
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBinding), SocketBinding.class, liveoakSocketBinding.socketBindingInjector())
                .install();


        PropertiesManagerService properties = new PropertiesManagerService();

        context.getServiceTarget().addService(name.append("properties"), properties)
                .addDependency(ServiceName.of("jboss", "server", "path", "jboss.home.dir"), String.class, properties.jbossHomeInjector())
                .install();

        ApplicationsDirectoryPathService appsDirPath = new ApplicationsDirectoryPathService();
        context.getServiceTarget().addService(name.append("apps-dir", "path"), appsDirPath)
                .addDependency(ServiceName.of("jboss", "server", "path", "jboss.home.dir"), String.class, appsDirPath.jbossHomeInjector())
                .install();

        TenancyBootstrappingService tenancy = new TenancyBootstrappingService();
        context.getServiceTarget().addService(name.append("tenancy"), tenancy)
                .addDependency(name.append("apps-dir", "path"), String.class, tenancy.applicationsDirectoryInjector())
                .addDependency(name.append("mongo-autosetup" ))
                .install();

        context.getServiceTarget().addService(name.append("servers"), new ServersBootstrappingService()).install();
        context.getServiceTarget().addService(name.append("codecs"), new CodecBootstrappingService()).install();
        context.getServiceTarget().addService(name.append("client"), new ClientBootstrappingService()).install();

        ExtensionsDirectoryPathService extsDirPath = new ExtensionsDirectoryPathService();
        context.getServiceTarget().addService(name.append("exts-dir", "path"), extsDirPath)
                .addDependency(ServiceName.of("jboss", "server", "path", "jboss.home.dir"), String.class, extsDirPath.jbossHomeInjector())
                .install();

        ExtensionsBootstrappingService extensions = new ExtensionsBootstrappingService();
        context.getServiceTarget().addService(name.append("extensions"), extensions)
                .addDependency(name.append("exts-dir", "path"), String.class, extensions.extensionsDirectoryInjector())
                .addDependency(name.append("properties"))
                .addDependency(name.append("mongo-autosetup" ))
                .install();

        context.getServiceTarget().addService(LiveOak.SERVICE_REGISTRY, new ValueService<ServiceRegistry>(new ImmediateValue<>(context.getServiceRegistry(false))))
                .install();

        context.getServiceTarget().addService(name.append("vertx"), new VertxBootstrappingService())
                .install();

        // ----------------------------------------


    }
}
