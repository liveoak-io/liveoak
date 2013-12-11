package io.liveoak.container.service;

import io.liveoak.container.NotifierImpl;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.async.Notifier;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class NotifierService implements Service<Notifier> {

    @Override
    public void start(StartContext context) throws StartException {
        this.notifier = new NotifierImpl( this.subscriptionManagerInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Notifier getValue() throws IllegalStateException, IllegalArgumentException {
        return this.notifier;
    }

    public Injector<SubscriptionManager> subscriptionManagerInjector() {
        return this.subscriptionManagerInjector;
    }

    private InjectedValue<SubscriptionManager> subscriptionManagerInjector = new InjectedValue<>();

    private Notifier notifier;

}
