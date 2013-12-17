/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols;

import java.util.concurrent.Executor;

import io.liveoak.client.DefaultClient;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.protocol.DebugHandler;
import io.liveoak.container.ErrorHandler;
import io.liveoak.container.ResourceHandler;
import io.liveoak.container.auth.AuthHandler;
import io.liveoak.container.auth.AuthzHandler;
import io.liveoak.container.deploy.ConfigurationWatcher;
import io.liveoak.container.deploy.DirectoryDeploymentManager;
import io.liveoak.container.interceptor.InterceptorHandler;
import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.container.protocols.http.HttpResourceRequestDecoder;
import io.liveoak.container.protocols.http.HttpResourceResponseEncoder;
import io.liveoak.container.protocols.local.LocalResourceResponseEncoder;
import io.liveoak.container.protocols.websocket.WebSocketHandshakerHandler;
import io.liveoak.container.protocols.websocket.WebSocketStompFrameDecoder;
import io.liveoak.container.protocols.websocket.WebSocketStompFrameEncoder;
import io.liveoak.container.subscriptions.ContainerStompServerContext;
import io.liveoak.container.subscriptions.SubscriptionWatcher;
import io.liveoak.spi.Container;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.stomp.common.StompFrameDecoder;
import io.liveoak.stomp.common.StompFrameEncoder;
import io.liveoak.stomp.common.StompMessageDecoder;
import io.liveoak.stomp.common.StompMessageEncoder;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.protocol.ConnectHandler;
import io.liveoak.stomp.server.protocol.DisconnectHandler;
import io.liveoak.stomp.server.protocol.ReceiptHandler;
import io.liveoak.stomp.server.protocol.SendHandler;
import io.liveoak.stomp.server.protocol.SubscribeHandler;
import io.liveoak.stomp.server.protocol.UnsubscribeHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

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

    public void container(Container container) {
        this.container = container;
    }

    public Container container() {
        return this.container;
    }

    public void deploymentManager(DirectoryDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public DirectoryDeploymentManager deploymentManager() {
        return this.deploymentManager;
    }

    public void interceptorManager(InterceptorManager interceptorManager) {
        this.interceptorManager  = interceptorManager;
    }

    public InterceptorManager interceptorManager() {
        return this.interceptorManager;
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
        pipeline.addLast(new ErrorHandler());

    }

    public void switchToHttpWebSockets(ChannelPipeline pipeline) {
        pipeline.remove(ProtocolDetector.class);
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024)); //TODO: Remove this to support chunked http
        pipeline.addLast(new WebSocketHandshakerHandler(this));
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
        pipeline.addLast(new ErrorHandler());
    }

    public void switchToPlainHttp(ChannelPipeline pipeline) {
        pipeline.remove(WebSocketHandshakerHandler.class);
        pipeline.addLast("http-resourceRead-decoder", new HttpResourceRequestDecoder(this.codecManager));
        pipeline.addLast("http-resourceRead-encoder", new HttpResourceResponseEncoder(this.codecManager));
        pipeline.addLast("interceptor", new InterceptorHandler( this.interceptorManager ) );

        if (container.hasResource("auth")) {
            pipeline.addLast("auth-handler", new AuthHandler(this.client));
        }

        if (container.hasResource("authz")) {
            pipeline.addLast("authz-handler", new AuthzHandler(this.client));
        }

        //pipeline.addLast( new DebugHandler( "networkServer-2" ) );
        if (this.deploymentManager != null) {
            pipeline.addLast("configuration-watcher", new ConfigurationWatcher(this.deploymentManager));
        }
        pipeline.addLast("subscription-watcher", new SubscriptionWatcher(this.subscriptionManager));
        pipeline.addLast("object-handler", new ResourceHandler(this.container, this.workerPool));
        pipeline.addLast("error-handler", new ErrorHandler());
    }

    public void setupLocal(ChannelPipeline pipeline) {
        //pipeline.addLast( new DebugHandler( "local-server-head" ) );
        pipeline.addLast( new LocalResourceResponseEncoder( this.workerPool) );
        pipeline.addLast("interceptor", new InterceptorHandler( this.interceptorManager ) );
        //pipeline.addLast( new DebugHandler( "upstream-from-interceptor" ) );
        pipeline.addLast(new SubscriptionWatcher(this.subscriptionManager));
        if (this.deploymentManager != null) {
            pipeline.addLast("configuration-watcher", new ConfigurationWatcher(this.deploymentManager));
        }
        pipeline.addLast(new ResourceHandler(this.container, this.workerPool));
    }

    private Client client;
    private ResourceCodecManager codecManager;
    private SubscriptionManager subscriptionManager;
    private DirectoryDeploymentManager deploymentManager;
    private InterceptorManager interceptorManager;
    private Executor workerPool;
    private Container container;

}
