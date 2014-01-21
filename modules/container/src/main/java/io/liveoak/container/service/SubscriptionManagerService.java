package io.liveoak.container.service;

import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class SubscriptionManagerService implements Service<DefaultSubscriptionManager> {
    @Override
    public void start(StartContext context) throws StartException {
        this.subscriptionManager = new DefaultSubscriptionManager();
    }

    @Override
    public void stop(StopContext context) {
        this.subscriptionManager = null;
    }

    @Override
    public DefaultSubscriptionManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.subscriptionManager;
    }

    private DefaultSubscriptionManager subscriptionManager;

}
