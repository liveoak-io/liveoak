package io.liveoak.container.service;

import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.server.AbstractNetworkServer;
import io.liveoak.spi.container.Address;
import io.liveoak.spi.container.NetworkServer;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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
            this.server.host( this.bindingInjector.getValue().getAddress() );
            this.server.port( this.bindingInjector.getValue().getPort() );
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
            log.error("", e);
        }
    }

    @Override
    public NetworkServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    public Injector<InetSocketAddress> bindingInjector() {
        return this.bindingInjector;
    }

    public Injector<PipelineConfigurator> pipelineConfiguratorInjector() {
        return this.pipelineConfiguratorInjector;
    }

    private AbstractNetworkServer server;

    private InjectedValue<InetSocketAddress> bindingInjector = new InjectedValue<>();
    private InjectedValue<PipelineConfigurator> pipelineConfiguratorInjector = new InjectedValue<>();

    private static final Logger log = Logger.getLogger(AbstractNetworkServerService.class);
}
