package io.liveoak.container.service;

import io.liveoak.client.DefaultClient;
import io.netty.channel.local.LocalAddress;
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
public class ClientConnectorService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        log.debug("connect client");
        try {
            this.clientInjector.getValue().connect(new LocalAddress("liveoak"));
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.clientInjector.getValue().close();
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<DefaultClient> clientInjector() {
        return this.clientInjector;
    }

    private InjectedValue<DefaultClient> clientInjector = new InjectedValue<>();

    private static final Logger log = Logger.getLogger(ClientConnectorService.class);
}
