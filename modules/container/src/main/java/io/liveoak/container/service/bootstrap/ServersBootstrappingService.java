package io.liveoak.container.service.bootstrap;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.interceptor.InterceptorManagerImpl;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.service.LocalServerService;
import io.liveoak.container.service.NotifierService;
import io.liveoak.container.service.PipelineConfiguratorService;
import io.liveoak.container.service.SubscriptionManagerService;
import io.liveoak.container.service.UnsecureServerService;
import io.liveoak.container.service.WorkerPoolService;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

import static io.liveoak.spi.Services.CLIENT;
import static io.liveoak.spi.Services.CODEC_MANAGER;
import static io.liveoak.spi.Services.CODEC_MANAGER_COMPLETE;
import static io.liveoak.spi.Services.GLOBAL_CONTEXT;
import static io.liveoak.spi.Services.INTERCEPTOR_MANAGER;
import static io.liveoak.spi.Services.NOTIFIER;
import static io.liveoak.spi.Services.PIPELINE_CONFIGURATOR;
import static io.liveoak.spi.Services.SOCKET_BINDING;
import static io.liveoak.spi.Services.SUBSCRIPTION_MANAGER;
import static io.liveoak.spi.Services.WORKER_POOL;
import static io.liveoak.spi.Services.server;

/**
 * @author Bob McWhirter
 */
public class ServersBootstrappingService implements Service<Void> {

    public ServersBootstrappingService() {
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        UnsecureServerService unsecureServer = new UnsecureServerService();
        target.addService(server("unsecure", true), unsecureServer)
                .addDependency(PIPELINE_CONFIGURATOR, PipelineConfigurator.class, unsecureServer.pipelineConfiguratorInjector())
                .addDependency(SOCKET_BINDING, InetSocketAddress.class, unsecureServer.bindingInjector())
                .install();

        LocalServerService localServer = new LocalServerService();
        target.addService(server("local", false), localServer)
                .addDependency(PIPELINE_CONFIGURATOR, PipelineConfigurator.class, localServer.pipelineConfiguratorInjector())
                .install();


        SubscriptionManagerService subscriptionManager = new SubscriptionManagerService();

        target.addService(SUBSCRIPTION_MANAGER, subscriptionManager)
                .addDependency(CLIENT, Client.class, subscriptionManager.clientInjector())
                .install();

        ValueService<InterceptorManagerImpl> interceptorManager = new ValueService<>(new ImmediateValue<InterceptorManagerImpl>(new InterceptorManagerImpl()));
        target.addService(INTERCEPTOR_MANAGER, interceptorManager)
                .install();

        WorkerPoolService workerPool = new WorkerPoolService();
        target.addService(WORKER_POOL, workerPool)
                .install();

        PipelineConfiguratorService pipelineConfigurator = new PipelineConfiguratorService();
        ServiceBuilder<PipelineConfigurator> pipelineBuilder = target.addService(PIPELINE_CONFIGURATOR, pipelineConfigurator)
                .addDependency(SUBSCRIPTION_MANAGER, SubscriptionManager.class, pipelineConfigurator.subscriptionManagerInjector())
                .addDependency(INTERCEPTOR_MANAGER, InterceptorManagerImpl.class, pipelineConfigurator.interceptorManagerInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, pipelineConfigurator.codecManagerInjector())
                .addDependency(CLIENT, Client.class, pipelineConfigurator.clientInjector())
                .addDependency(GLOBAL_CONTEXT, GlobalContext.class, pipelineConfigurator.globalContextInjector())
                .addDependency(WORKER_POOL, Executor.class, pipelineConfigurator.workerPoolInjector())
                .addDependency(CODEC_MANAGER_COMPLETE);

        pipelineBuilder.install();


        NotifierService notifier = new NotifierService();
        target.addService(NOTIFIER, notifier)
                .addDependency(SUBSCRIPTION_MANAGER, SubscriptionManager.class, notifier.subscriptionManagerInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

}
