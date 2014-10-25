/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpResponse;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsResponseHandler extends ChannelOutboundHandlerAdapter {

    private final AnalyticsBandwidthHandler analyticsHandler;

    public AnalyticsResponseHandler(AnalyticsBandwidthHandler analyticsHandler) {
        this.analyticsHandler = analyticsHandler;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof DefaultHttpResponse) {
            analyticsHandler.httpResponseInfo((DefaultHttpResponse) msg);
        }
        super.write(ctx, msg, promise);
    }

}
