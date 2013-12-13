package io.liveoak.container.service;

import io.liveoak.client.DefaultClient;
import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.client.Client;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ClientService implements Service<Client> {

    @Override
    public void start(StartContext context) throws StartException {
        this.client = new DefaultClient();
        try {
            this.client.connect( this.serverInjector.getValue().localAddress() );
        } catch (Exception e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        this.client.close();
    }

    @Override
    public Client getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }

    public Injector<LocalServer> serverInjector() {
        return this.serverInjector;
    }

    private DefaultClient client;
    private InjectedValue<LocalServer> serverInjector = new InjectedValue<>();
}
