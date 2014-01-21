package io.liveoak.container.subscriptions;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.Subscription;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Bob McWhirter
 */
public class SubscriptionTree {

    public SubscriptionTree(String id) {
        this.id = id;
    }

    void addSubscription(Subscription subscription) {
        SubscriptionTree leaf = findLeaf(subscription.resourcePath());
        leaf.subscriptions.add(subscription);
    }

    void removeSubscription(Subscription subscription) {
        SubscriptionTree leaf = findLeaf(subscription.resourcePath());
        leaf.subscriptions.remove(subscription);
    }

    SubscriptionTree findLeaf(ResourcePath path) {
        if (path.isEmpty()) {
            return this;
        }
        String id = path.head().name();

        SubscriptionTree child = this.children.get(id);
        if (child == null) {
            child = new SubscriptionTree(id);
            this.children.put(path.head().name(), child);
        }

        return child.findLeaf(path.subPath());
    }

    Stream<Subscription> subscriptions(ResourcePath path) {
        if (path.isEmpty()) {
            return this.subscriptions.stream();
        }

        String name = path.head().name();

        SubscriptionTree wildcardChild = this.children.get("*");
        Stream<Subscription> wildcardSubs = null;
        if ( wildcardChild != null ) {
            wildcardSubs = wildcardChild.subscriptions( path.subPath() );
        }

        SubscriptionTree child = this.children.get(name);
        Stream<Subscription> childSubs = null;

        if (child != null) {
            childSubs = child.subscriptions(path.subPath());
        }

        if ( wildcardSubs != null && childSubs != null ) {
            return Stream.concat( wildcardSubs, childSubs );
        }

        if ( wildcardSubs != null ) {
            return wildcardSubs;
        }

        if ( childSubs != null ) {
            return childSubs;
        }

        List<Subscription> empty = Collections.emptyList();
        return empty.stream();
    }

    public Stream<Subscription> subscriptions() {
        return Stream.concat(this.subscriptions.stream(),
                this.children.values().stream().flatMap((e) -> {
                    return e.subscriptions();
                }));
    }

    private String id;
    private Map<String, SubscriptionTree> children = new HashMap<>();
    private List<Subscription> subscriptions = new ArrayList<>();

}
