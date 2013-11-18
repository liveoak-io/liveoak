/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions.resource;

import io.liveoak.container.aspects.ResourceAspect;
import io.liveoak.container.subscriptions.SubscriptionManager;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class SubscriptionsResourceAspect implements ResourceAspect {

    public SubscriptionsResourceAspect( SubscriptionManager subscriptionManager ) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public Resource forResource( Resource resource ) {
        if ( resource instanceof SubscriptionsCollectionResource ) {
            return null;
        }

        if ( resource instanceof SubscriptionResource ) {
            return null;
        }

        return new SubscriptionsCollectionResource( resource, this.subscriptionManager );
        //return new SimplePropertyResource( resource, "_subscriptions", new SubscriptionsCollectionResource( resource, this.subscriptionManager ) );
    }

    private SubscriptionManager subscriptionManager;
}
