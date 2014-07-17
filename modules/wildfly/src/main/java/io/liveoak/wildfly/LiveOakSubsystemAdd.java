package io.liveoak.wildfly;

import java.util.List;

import io.liveoak.container.service.bootstrap.ClientBootstrappingService;
import io.liveoak.container.service.bootstrap.CodecBootstrappingService;
import io.liveoak.container.service.bootstrap.ExtensionsBootstrappingService;
import io.liveoak.container.service.bootstrap.ServersBootstrappingService;
import io.liveoak.container.service.bootstrap.TenancyBootstrappingService;
import io.liveoak.container.service.bootstrap.VertxBootstrappingService;
import io.liveoak.mongo.launcher.service.MongoLauncherAutoSetupService;
import io.liveoak.spi.LiveOak;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 */
public class LiveOakSubsystemAdd extends AbstractBoottimeAddStepHandler {
    static final LiveOakSubsystemAdd INSTANCE = new LiveOakSubsystemAdd();

    static final ServiceName JBOSS_HOME = ServiceName.of("jboss", "server", "path", "jboss.home.dir");
    static final ServiceName LIVEOAK_SUB = LiveOak.LIVEOAK.append("wildfly", "subsystem");
    static final String LIVEOAK_HOME_PROPERTY = "io.liveoak.home.dir";
    static final ServiceName LIVEOAK_HOME = LIVEOAK_SUB.append("path", LIVEOAK_HOME_PROPERTY);
    static final ServiceName CONF_PATH = LIVEOAK_SUB.append("conf-dir", "path");
    static final ServiceName EXTS_PATH = LIVEOAK_SUB.append("exts-dir", "path");
    static final ServiceName APPS_PATH = LIVEOAK_SUB.append("apps-dir", "path");


    private static final Logger log = Logger.getLogger(LiveOakSubsystemAdd.class);

    private LiveOakSubsystemAdd() {
    }

    @Override
    protected void populateModel(OperationContext context, ModelNode operation, Resource resource) throws OperationFailedException {
        log.debug("OP: " + operation);
        log.debug("binding: " + operation.get("socket-binding"));
        ModelNode node = new ModelNode();
        node.get("socket-binding").set(operation.get("socket-binding"));
        resource.getModel().set(node);
        log.debug("resource: " + resource.getModel());
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

        log.trace("BOOT MODEL: " + model);
        log.trace("BOOT OP: " + operation);


        String propVal = System.getProperty(LIVEOAK_HOME_PROPERTY);

        // check that directory exists?
        PathService liveOakPathService = new PathService(LIVEOAK_HOME, propVal);
        ServiceBuilder builder = context.getServiceTarget().addService(LIVEOAK_HOME, liveOakPathService);
        if (propVal == null) {
            builder.addDependency(JBOSS_HOME, liveOakPathService.parentPathInjector());
        } else {
            liveOakPathService.parentPathInjector().inject(null);
        }
        builder.install();

        PathService confDirPath = new PathService("conf");
        context.getServiceTarget().addService(CONF_PATH, confDirPath)
                .addDependency(LIVEOAK_HOME, String.class, confDirPath.parentPathInjector())
                .install();

        MongoLauncherAutoSetupService mongo = new MongoLauncherAutoSetupService();

        context.getServiceTarget().addService(LIVEOAK_SUB.append("mongo-autosetup"), mongo)
                .addDependency(CONF_PATH, String.class, mongo.extensionsDirInjector())
                .addDependency(LIVEOAK_HOME, String.class, mongo.liveoakDirInjector())
                .install();

        log.debug("installed mongo auto-setup");

        String socketBinding = model.get("socket-binding").asString();

        LiveOakSocketBindingService liveoakSocketBinding = new LiveOakSocketBindingService();

        context.getServiceTarget().addService(LiveOak.SOCKET_BINDING, liveoakSocketBinding)
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBinding), SocketBinding.class, liveoakSocketBinding.socketBindingInjector())
                .install();


        PropertiesManagerService properties = new PropertiesManagerService();

        context.getServiceTarget().addService(LIVEOAK_SUB.append("properties"), properties)
                .addDependency(LIVEOAK_HOME, String.class, properties.jbossHomeInjector())
                .install();

        PathService appsDirPath = new PathService("apps");
        context.getServiceTarget().addService(APPS_PATH, appsDirPath)
                .addDependency(LIVEOAK_HOME, String.class, appsDirPath.parentPathInjector())
                .install();

        TenancyBootstrappingService tenancy = new TenancyBootstrappingService();
        context.getServiceTarget().addService(LIVEOAK_SUB.append("tenancy"), tenancy)
                .addDependency(APPS_PATH, String.class, tenancy.applicationsDirectoryInjector())
                .addDependency(LIVEOAK_SUB.append("mongo-autosetup"))
                .install();

        context.getServiceTarget().addService(LIVEOAK_SUB.append("servers"), new ServersBootstrappingService()).install();
        context.getServiceTarget().addService(LIVEOAK_SUB.append("codecs"), new CodecBootstrappingService()).install();
        context.getServiceTarget().addService(LIVEOAK_SUB.append("client"), new ClientBootstrappingService()).install();

        PathService extsDirPath = new PathService("conf/extensions");
        context.getServiceTarget().addService(EXTS_PATH, extsDirPath)
                .addDependency(LIVEOAK_HOME, String.class, extsDirPath.parentPathInjector())
                .install();

        ExtensionsBootstrappingService extensions = new ExtensionsBootstrappingService();
        context.getServiceTarget().addService(LIVEOAK_SUB.append("extensions"), extensions)
                .addDependency(EXTS_PATH, String.class, extensions.extensionsDirectoryInjector())
                .addDependency(LIVEOAK_SUB.append("properties"))
                .addDependency(LIVEOAK_SUB.append("mongo-autosetup"))
                .install();

        context.getServiceTarget().addService(LiveOak.SERVICE_REGISTRY, new ValueService<ServiceRegistry>(new ImmediateValue<>(context.getServiceRegistry(false))))
                .install();

        context.getServiceTarget().addService(LIVEOAK_SUB.append("vertx"), new VertxBootstrappingService())
                .install();
    }
}
