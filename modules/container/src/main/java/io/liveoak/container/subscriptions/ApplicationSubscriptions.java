package io.liveoak.container.subscriptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.liveoak.spi.container.Subscription;

/**
 * @author Bob McWhirter
 */
public class ApplicationSubscriptions {

    public ApplicationSubscriptions() {

    }

    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        this.subscriptions.remove(subscription);
    }

    public Collection<Subscription> subscriptions() {
        return this.subscriptions;
    }

    private List<Subscription> subscriptions = new ArrayList<>();
}
