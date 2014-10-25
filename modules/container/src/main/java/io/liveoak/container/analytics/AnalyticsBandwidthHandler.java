/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.analytics;

import java.net.SocketAddress;

import io.liveoak.container.protocols.RequestCompleteEvent;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsBandwidthHandler extends ChannelDuplexHandler {

    private long bytesRead;
    private long bytesWritten;
    private long startTime;
    private long lastTime;

    private AnalyticsEvent event;
    private AnalyticsService analyticsService;

    private static final Logger log = Logger.getLogger(AnalyticsBandwidthHandler.class);

    public AnalyticsBandwidthHandler(AnalyticsService service) {
        this.analyticsService = service;
    }

    public void httpResponseInfo(DefaultHttpResponse response) {
        if (event != null) {
            event.setStatus(response.getStatus().code());
        }
    }

    public void notificationInfo(StompMessage msg) {
        AnalyticsEvent ev = new AnalyticsEvent();
        Headers headers = msg.headers();
        String status = headers.get("status");
        if (status != null) {
            try {
                ev.setStatus(Integer.valueOf(status));
            } catch (Exception e) {
                log.error("[IGNORED] Failed to parse STOMP notification status: " + status);
            }
        }

        String location = headers.get("location");
        ev.setUri(location);
        ev.setNotification(headers.get("action"));
        ev.setTimestamp(System.currentTimeMillis());

        if (location != null && location.length() > 0) {

            if (location.startsWith("/")) {
                location = location.substring(1);
            }
            int last = location.indexOf("/");
            if (last == -1) {
                last = location.length();
            }
            ev.setApplication(location.substring(0, last));
        }

        ev.setApiRequest(false);
        if (event != null) {
            ev.setClientAddress(event.getClientAddress());
        }

        // push notification event
        analyticsService.event(ev);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            bytesRead += ((ByteBuf) msg).readableBytes();
            startTime = startTime != 0 ? startTime : System.currentTimeMillis();

        } else if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest req = (DefaultHttpRequest) msg;
            event = new AnalyticsEvent();

            startTime = startTime != 0 ? startTime : System.currentTimeMillis();
            event.setTimestamp(startTime);
            event.setMethod(req.getMethod().name());
            event.setUri(req.getUri());
            event.clientAddress(ctx.channel().remoteAddress());
            //event.uri(req.resourcePath().toString());
        } else if (msg instanceof ResourceRequest) {
            ResourceRequest req = (ResourceRequest) msg;
            event.setApplication(req.resourcePath().head().toString());
            event.setUserId(req.requestContext().securityContext().getSubject());
            event.setApiRequest(true);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("Request byte count: " + bytesRead);
        //if (bytesRead > 0 && event != null) {
        //    event.getRequestBytes(bytesRead);
        //    bytesRead = 0;
        //}
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof RequestCompleteEvent) {
            //System.out.println("Response byte count: " + bytesWritten);
            pushEvent();
        }
        super.userEventTriggered(ctx, evt);
    }

    private void pushEvent() {
        if (event == null) {
            return;
        }
        event.setRequestBytes(bytesRead);
        event.setResponseBytes(bytesWritten);
        lastTime = lastTime != 0 ? lastTime : System.currentTimeMillis();
        event.setDuration(lastTime - startTime);
        analyticsService.event(event);

        bytesRead = 0;
        bytesWritten = 0;
        startTime = 0;
        lastTime = 0;
        event = null;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            bytesWritten += ((ByteBuf) msg).readableBytes();
            lastTime = System.currentTimeMillis();
        }
        super.write(ctx, msg, promise);
    }


    // other methods ...


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future) throws Exception {
        super.bind(ctx, localAddress, future);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        super.disconnect(ctx, future);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        super.close(ctx, future);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        super.deregister(ctx, future);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        pushEvent();
        super.handlerRemoved(ctx);
    }
}
