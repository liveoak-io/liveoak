/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions;

import io.liveoak.container.DefaultResourceResponse;
import io.liveoak.spi.container.SubscriptionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;


/**
 * @author Bob McWhirter
 */
public class SubscriptionWatcher extends ChannelOutboundHandlerAdapter {

    public SubscriptionWatcher(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof DefaultResourceResponse) {
            DefaultResourceResponse response = (DefaultResourceResponse) msg;

            switch (response.responseType()) {
                case CREATED:
                    this.subscriptionManager.resourceCreated(response.resource());
                    break;
                case READ:
                    // no notification
                    break;
                case UPDATED:
                    this.subscriptionManager.resourceUpdated(response.resource());
                    break;
                case DELETED:
                    this.subscriptionManager.resourceDeleted(response.resource());
                    break;
            }
        }

        super.write(ctx, msg, promise);
    }

    private SubscriptionManager subscriptionManager;

}
