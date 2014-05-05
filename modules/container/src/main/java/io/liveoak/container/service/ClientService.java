package io.liveoak.container.service;

import io.liveoak.client.DefaultClient;
import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.client.Client;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ClientService implements Service<DefaultClient> {

    @Override
    public void start(StartContext context) throws StartException {
        log.debug("start client");
        this.client = new DefaultClient();
    }

    @Override
    public void stop(StopContext context) {
        //this.client.close();
    }

    @Override
    public DefaultClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }

    private DefaultClient client;

    private static final Logger log = Logger.getLogger(ClientService.class);
}
