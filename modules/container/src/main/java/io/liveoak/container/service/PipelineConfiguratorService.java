package io.liveoak.container.service;

import java.util.concurrent.Executor;

import io.liveoak.container.deploy.DirectoryDeploymentManager;
import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.spi.Container;
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
        this.pipelineConfigurator.codecManager( this.codecManagerInjector.getValue() );
        this.pipelineConfigurator.subscriptionManager( this.subscriptionManagerInjector.getValue() );
        this.pipelineConfigurator.workerPool( this.workerPoolInjector.getValue() );
        this.pipelineConfigurator.container( this.containerInjector.getValue() );
        this.pipelineConfigurator.interceptorManager( this.interceptorManagerInjector.getValue() );
        this.pipelineConfigurator.deploymentManager( this.deploymentManagerInjector.getOptionalValue() );
    }

    @Override
    public void stop(StopContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PipelineConfigurator getValue() throws IllegalStateException, IllegalArgumentException {
        return this.pipelineConfigurator;
    }

    public Injector<Container> containerInjector() {
        return this.containerInjector;
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

    public Injector<DirectoryDeploymentManager> deploymentManagerInjector() {
        return this.deploymentManagerInjector;
    }

    public Injector<InterceptorManager> interceptorManagerInjector() {
        return this.interceptorManagerInjector;
    }

    private PipelineConfigurator pipelineConfigurator;

    private InjectedValue<Container> containerInjector = new InjectedValue<>();
    private InjectedValue<ResourceCodecManager> codecManagerInjector = new InjectedValue<>();
    private InjectedValue<SubscriptionManager> subscriptionManagerInjector = new InjectedValue<>();
    private InjectedValue<Executor> workerPoolInjector = new InjectedValue<>();
    private InjectedValue<DirectoryDeploymentManager> deploymentManagerInjector = new InjectedValue<>();
    private InjectedValue<InterceptorManager> interceptorManagerInjector = new InjectedValue<>();


}
