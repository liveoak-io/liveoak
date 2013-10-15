package org.projectodd.restafari.container;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.projectodd.restafari.container.CollectionHandler;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ContainerStompServerContext;
import org.projectodd.restafari.container.ResourceHandler;
import org.projectodd.restafari.container.protocols.ProtocolDetector;
import org.projectodd.restafari.container.protocols.http.*;
import org.projectodd.restafari.container.subscriptions.SubscriptionWatcher;
import org.projectodd.restafari.stomp.common.*;
import org.projectodd.restafari.container.protocols.websocket.WebSocketHandshakerHandler;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameDecoder;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameEncoder;
import org.projectodd.restafari.stomp.server.StompServerContext;
import org.projectodd.restafari.stomp.server.protocol.*;

/**
 * @author Bob McWhirter
 */
public class PipelineConfigurator {

    public PipelineConfigurator(Container container) {
        this.container = container;
    }

    public void switchToPureStomp(ChannelPipeline pipeline) {
        pipeline.remove(ProtocolDetector.class);

        StompServerContext serverContext = new ContainerStompServerContext(this.container);

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
        pipeline.addLast(new WebSocketStompFrameDecoder());
        pipeline.addLast(new WebSocketStompFrameEncoder());
    }

    public void switchToPlainHttp(ChannelPipeline pipeline) {
        pipeline.remove(WebSocketHandshakerHandler.class);
        //pipeline.addLast( new DebugHandler( "server-1" ) );
        pipeline.addLast("http-resource-decoder", new HttpResourceRequestDecoder(this.container.getCodecManager()));
        pipeline.addLast("http-resource-encoder", new HttpResourceResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast("http-collection-encoder", new HttpCollectionResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast("http-error-encoder", new HttpErrorResponseEncoder());
        //pipeline.addLast( new DebugHandler( "server-2" ) );
        pipeline.addLast("subscription-watcher", new SubscriptionWatcher(this.container.getSubscriptionManager()));
        pipeline.addLast("container-resource-handler", new ResourceHandler(this.container));
        pipeline.addLast("container-collection-handler", new CollectionHandler(this.container));
    }

    private Container container;
}
