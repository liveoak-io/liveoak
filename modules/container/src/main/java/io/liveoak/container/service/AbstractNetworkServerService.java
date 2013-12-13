package io.liveoak.container.service;

import java.net.InetAddress;

import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.server.AbstractNetworkServer;
import io.liveoak.container.server.AbstractServer;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.Container;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractNetworkServerService implements Service<NetworkServer> {

    public AbstractNetworkServerService() {
    }

    public abstract AbstractNetworkServer newServer();

    @Override
    public void start(StartContext context) throws StartException {
        this.server = newServer();
        try {
            this.server.host( this.hostInjector.getValue() );
            this.server.port( this.portInjector.getValue() );
            this.server.pipelineConfigurator( this.pipelineConfiguratorInjector.getValue() );
            this.server.start();
        } catch (Exception e) {
            context.failed( new StartException(e) );
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NetworkServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    public Injector<InetAddress> hostInjector() {
        return this.hostInjector;
    }

    public Injector<Integer> portInjector() {
        return this.portInjector;
    }

    public Injector<PipelineConfigurator> pipelineConfiguratorInjector() {
        return this.pipelineConfiguratorInjector;
    }

    private AbstractNetworkServer server;

    private InjectedValue<InetAddress> hostInjector = new InjectedValue<>();
    private InjectedValue<Integer> portInjector = new InjectedValue<>();
    private InjectedValue<PipelineConfigurator> pipelineConfiguratorInjector = new InjectedValue<>();
}
