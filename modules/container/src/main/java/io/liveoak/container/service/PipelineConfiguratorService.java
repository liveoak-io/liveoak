package io.liveoak.container.service;

import java.util.concurrent.Executor;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.interceptor.InterceptorManagerImpl;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class PipelineConfiguratorService implements Service<PipelineConfigurator> {
    @Override
    public void start(StartContext context) throws StartException {
        this.pipelineConfigurator = new PipelineConfigurator();
        this.pipelineConfigurator.globalContext(this.globalContextInjector.getValue());
        this.pipelineConfigurator.codecManager(this.codecManagerInjector.getValue());
        this.pipelineConfigurator.subscriptionManager(this.subscriptionManagerInjector.getValue());
        this.pipelineConfigurator.workerPool(this.workerPoolInjector.getValue());
        this.pipelineConfigurator.interceptorManager(this.interceptorManagerInjector.getValue());
        this.pipelineConfigurator.client(this.clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PipelineConfigurator getValue() throws IllegalStateException, IllegalArgumentException {
        return this.pipelineConfigurator;
    }

    public Injector<ResourceCodecManager> codecManagerInjector() {
        return this.codecManagerInjector;
    }

    public Injector<SubscriptionManager> subscriptionManagerInjector() {
        return this.subscriptionManagerInjector;
    }

    public Injector<Executor> workerPoolInjector() {
        return this.workerPoolInjector;
    }

    public Injector<InterceptorManagerImpl> interceptorManagerInjector() {
        return this.interceptorManagerInjector;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    public Injector<GlobalContext> globalContextInjector() {
        return this.globalContextInjector;
    }

    private PipelineConfigurator pipelineConfigurator;

    private InjectedValue<GlobalContext> globalContextInjector = new InjectedValue<>();
    private InjectedValue<ResourceCodecManager> codecManagerInjector = new InjectedValue<>();
    private InjectedValue<SubscriptionManager> subscriptionManagerInjector = new InjectedValue<>();
    private InjectedValue<Executor> workerPoolInjector = new InjectedValue<>();
    private InjectedValue<InterceptorManagerImpl> interceptorManagerInjector = new InjectedValue<>();
    private InjectedValue<Client> clientInjector = new InjectedValue<>();


}
