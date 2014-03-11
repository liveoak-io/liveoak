package io.liveoak.container.service;

import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.container.subscriptions.SecuredSubscriptionManager;
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
public class SubscriptionManagerService implements Service<DefaultSubscriptionManager> {
    @Override
    public void start(StartContext context) throws StartException {
        this.subscriptionManager = new SecuredSubscriptionManager(clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.subscriptionManager = null;
    }

    @Override
    public DefaultSubscriptionManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.subscriptionManager;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    private DefaultSubscriptionManager subscriptionManager;
    private InjectedValue<Client> clientInjector = new InjectedValue<>();

}
