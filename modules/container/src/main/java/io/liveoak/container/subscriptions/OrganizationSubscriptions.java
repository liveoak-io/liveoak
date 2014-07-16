package io.liveoak.container.subscriptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.container.Subscription;

/**
 * @author Bob McWhirter
 */
public class OrganizationSubscriptions {

    public OrganizationSubscriptions() {

    }

    public Collection<Subscription> subscriptions(String appId) {
        ApplicationSubscriptions subscriptions = this.applicationSubscriptions.get(appId);
        if (subscriptions == null) {
            return Collections.emptyList();
        }

        return subscriptions.subscriptions();
    }

    void addSubscription(Subscription subscription) {
        ApplicationSubscriptions subscriptions = findOrCreateApplication(subscription);
        subscriptions.addSubscription(subscription);
    }

    void removeSubscription(Subscription subscription) {
        ApplicationSubscriptions subscriptions = findApplication(subscription);
        if (subscription == null) {
            return;
        }
        subscriptions.removeSubscription(subscription);
    }

    ApplicationSubscriptions findOrCreateApplication(Subscription subscription) {
        String appId = subscription.resourcePath().segments().get(1).name();
        ApplicationSubscriptions subscriptions = findApplication(appId);
        if (subscriptions == null) {
            subscriptions = new ApplicationSubscriptions();
            this.applicationSubscriptions.put(appId, subscriptions);
        }

        return subscriptions;
    }

    ApplicationSubscriptions findApplication(Subscription subscription) {
        String appId = subscription.resourcePath().segments().get(1).name();
        return findApplication(appId);
    }

    ApplicationSubscriptions findApplication(String appId) {
        return this.applicationSubscriptions.get(appId);
    }

    private Map<String, ApplicationSubscriptions> applicationSubscriptions = new HashMap<>();
}
