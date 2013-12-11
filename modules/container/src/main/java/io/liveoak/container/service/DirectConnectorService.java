package io.liveoak.container.service;

import java.util.concurrent.Executor;

import io.liveoak.container.DefaultDirectConnector;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.DirectConnector;
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
public class DirectConnectorService implements Service<DirectConnector> {

    @Override
    public void start(StartContext context) throws StartException {
        this.connector = new DefaultDirectConnector( this.pipelineConfiguratorInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public DirectConnector getValue() throws IllegalStateException, IllegalArgumentException {
        return this.connector;
    }

    public Injector<PipelineConfigurator> pipelineConfiguratorInjector() {
        return this.pipelineConfiguratorInjector;
    }

    private DefaultDirectConnector connector;
    private InjectedValue<PipelineConfigurator> pipelineConfiguratorInjector = new InjectedValue<>();
}
