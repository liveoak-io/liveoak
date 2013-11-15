/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions.resource;

import io.liveoak.container.subscriptions.Subscription;
import io.liveoak.container.subscriptions.SubscriptionManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class SubscriptionsCollectionResource implements Resource {

    public SubscriptionsCollectionResource(Resource parent, SubscriptionManager subscriptionManager) {
        this.parent = parent;
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        Stream<Subscription> subscriptions = this.subscriptionManager.getSubscriptions(this.parent);
        subscriptions.forEach((each) -> {
            sink.accept(new SubscriptionResource(this, each));
        });
        sink.close();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "_subscriptions";
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        Subscription subscription = this.subscriptionManager.getSubscription(this.parent, id);
        if (subscription == null) {
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(new SubscriptionResource(this, subscription));
    }

    private Resource parent;
    private SubscriptionManager subscriptionManager;
}
