package io.liveoak.container.service;

import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
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
public class SubscriptionManagerService implements Service<SubscriptionManager> {
    @Override
    public void start(StartContext context) throws StartException {
        this.subscriptionManager = new DefaultSubscriptionManager( this.idInjector.getValue(), this.codecManagerInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.subscriptionManager = null;
    }

    @Override
    public SubscriptionManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.subscriptionManager;
    }

    public Injector<ResourceCodecManager> codecManagerInjector() {
        return this.codecManagerInjector;
    }

    public Injector<String> idInjector() {
        return this.idInjector;
    }

    private SubscriptionManager subscriptionManager;

    private InjectedValue<String> idInjector = new InjectedValue<>();
    private InjectedValue<ResourceCodecManager> codecManagerInjector = new InjectedValue<>();
}
