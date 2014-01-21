/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.protocol.DebugHandler;
import io.liveoak.container.ErrorHandler;
import io.liveoak.container.ResourceHandler;
import io.liveoak.container.ResourceStateHandler;
import io.liveoak.container.auth.AuthHandler;
import io.liveoak.container.auth.AuthzHandler;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.container.interceptor.InterceptorHandler;
import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.container.protocols.http.HttpRequestBodyHandler;
import io.liveoak.container.protocols.http.HttpResourceRequestDecoder;
import io.liveoak.container.protocols.http.HttpResourceResponseEncoder;
import io.liveoak.container.protocols.local.LocalResourceResponseEncoder;
import io.liveoak.container.protocols.websocket.WebSocketHandshakerHandler;
import io.liveoak.container.protocols.websocket.WebSocketStompFrameDecoder;
import io.liveoak.container.protocols.websocket.WebSocketStompFrameEncoder;
import io.liveoak.container.subscriptions.ContainerStompServerContext;
import io.liveoak.container.subscriptions.SubscriptionWatcher;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.stomp.common.StompFrameDecoder;
import io.liveoak.stomp.common.StompFrameEncoder;
import io.liveoak.stomp.common.StompMessageDecoder;
import io.liveoak.stomp.common.StompMessageEncoder;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.protocol.*;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class PipelineConfigurator {

    public PipelineConfigurator() {
    }

    public void client(Client client) {
        this.client = client;
    }

    public Client client() {
        return this.client;
    }

    public void globalContext(GlobalContext globalContext) {
        this.globalContext = globalContext;
    }

    public GlobalContext globalContext() {
        return this.globalContext;
    }

    public void codecManager(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    public ResourceCodecManager codecManager() {
        return this.codecManager;
    }

    public void subscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public SubscriptionManager subscriptionManager() {
        return this.subscriptionManager;
    }

    public void workerPool(Executor workerPool) {
        this.workerPool = workerPool;
    }

    public Executor workerPool() {
        return this.workerPool;
    }

    public void interceptorManager(InterceptorManager interceptorManager) {
        this.interceptorManager = interceptorManager;
    }

    public InterceptorManager interceptorManager() {
        return this.interceptorManager;
    }

    public String tempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public void switchToPureStomp(ChannelPipeline pipeline) {
        pipeline.remove(ProtocolDetector.class);

        StompServerContext serverContext = new ContainerStompServerContext(this.codecManager, this.subscriptionManager);

        pipeline.addLast(new StompFrameDecoder());
        pipeline.addLast(new StompFrameEncoder());
        // handle frames
        pipeline.addLast(new ConnectHandler(serverContext));
        pipeline.addLast(new DisconnectHandler(serverContext));
        pipeline.addLast(new SubscribeHandler(serverContext));
        pipeline.addLast(new UnsubscribeHandler(serverContext));
        // convert some frames to messages
        pipeline.addLast(new ReceiptHandler());
        pipeline.addLast(new StompMessageDecoder());
        pipeline.addLast(new StompMessageEncoder(true));
        // handle messages
        pipeline.addLast(new SendHandler(serverContext));
        // catch errors, return an ERROR message.
        pipeline.addLast(new StompErrorHandler());

    }

    public void switchToHttpWebSockets(ChannelPipeline pipeline) {
        pipeline.remove(ProtocolDetector.class);
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        //pipeline.addLast(new HttpObjectAggregator(1024 * 1024)); //TODO: Remove this to support chunked http
        pipeline.addLast("ws-handshake", new WebSocketHandshakerHandler(this));
    }

    public void switchToWebSocketsHandshake(ChannelPipeline pipeline) {
        pipeline.addBefore("ws-handshake", "aggregator", new HttpObjectAggregator(1024 * 1024));
    }

    public void switchToWebSockets(ChannelPipeline pipeline) {
        pipeline.remove(WebSocketHandshakerHandler.class);
        //pipeline.addLast( new DebugHandler( "networkServer-1" ) );
        pipeline.addLast(new WebSocketStompFrameDecoder());
        pipeline.addLast(new WebSocketStompFrameEncoder());

        StompServerContext serverContext = new ContainerStompServerContext(this.codecManager, this.subscriptionManager);

        // handle frames
        pipeline.addLast(new ConnectHandler(serverContext));
        pipeline.addLast(new DisconnectHandler(serverContext));
        pipeline.addLast(new SubscribeHandler(serverContext));
        pipeline.addLast(new UnsubscribeHandler(serverContext));
        // convert some frames to messages
        pipeline.addLast(new ReceiptHandler());
        pipeline.addLast(new StompMessageDecoder());
        pipeline.addLast(new StompMessageEncoder(true));
        // handle messages
        pipeline.addLast(new SendHandler(serverContext));
        // catch errors, return an ERROR message.
        pipeline.addLast(new StompErrorHandler());
    }

    public void switchToPlainHttp(ChannelPipeline pipeline) {
        // turn off automatic socket read for better http body read control
        pipeline.channel().config().setAutoRead(false);
        pipeline.remove(WebSocketHandshakerHandler.class);
        pipeline.addLast("http-resource-decoder", new HttpResourceRequestDecoder(this.codecManager));
        pipeline.addLast("http-resource-encoder", new HttpResourceResponseEncoder(this.codecManager));
        pipeline.addLast("http-request-body-handler", new HttpRequestBodyHandler());
        pipeline.addLast("interceptor", new InterceptorHandler( this.interceptorManager ) );

        pipeline.addLast("auth-handler", new AuthHandler(this.client));
        pipeline.addLast("authz-handler", new AuthzHandler(this.client));

        pipeline.addLast("subscription-watcher", new SubscriptionWatcher(this.subscriptionManager));
        //pipeline.addLast( new DebugHandler( "server-debug" ) );
        pipeline.addLast("resource-state-handler", new ResourceStateHandler(this.workerPool));
        pipeline.addLast("object-handler", new ResourceHandler(this.globalContext, this.workerPool));
        pipeline.addLast("error-handler", new ErrorHandler());
    }

    public void setupLocal(ChannelPipeline pipeline) {
        //pipeline.addLast( new DebugHandler( "local-head" ) );
        pipeline.addLast(new LocalResourceResponseEncoder(this.workerPool));
        pipeline.addLast("interceptor", new InterceptorHandler(this.interceptorManager));
        pipeline.addLast(new SubscriptionWatcher(this.subscriptionManager));
        pipeline.addLast(new ResourceStateHandler(this.workerPool));
        pipeline.addLast(new ResourceHandler(this.globalContext, this.workerPool));
        //pipeline.addLast( new DebugHandler( "local-tail" ) );
    }

    private Client client;
    private GlobalContext globalContext;
    private ResourceCodecManager codecManager;
    private SubscriptionManager subscriptionManager;
    private InterceptorManager interceptorManager;
    private Executor workerPool;

}
