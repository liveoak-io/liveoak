package io.liveoak.container.service;

import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.server.AbstractNetworkServer;
import io.liveoak.container.server.LocalServer;
import io.liveoak.container.server.UnsecureServer;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class LocalServerService implements Service<LocalServer> {

    @Override
    public void start(StartContext context) throws StartException {
        this.server = new LocalServer();
        this.server.pipelineConfigurator(this.pipelineConfiguratorInjector.getValue());
        try {
            this.server.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        try {
            this.server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LocalServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    public Injector<PipelineConfigurator> pipelineConfiguratorInjector() {
        return this.pipelineConfiguratorInjector;
    }

    private InjectedValue<PipelineConfigurator> pipelineConfiguratorInjector = new InjectedValue<>();

    private LocalServer server;

}
