package org.projectodd.restafari.container.protocols;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ContainerHandler;
import org.projectodd.restafari.container.ContainerStompServerContext;
import org.projectodd.restafari.container.protocols.http.*;
import org.projectodd.restafari.stomp.common.StompFrameDecoder;
import org.projectodd.restafari.stomp.common.StompFrameEncoder;
import org.projectodd.restafari.container.protocols.websocket.WebSocketHandshakerHandler;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameDecoder;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameEncoder;
import org.projectodd.restafari.stomp.common.StompMessageDecoder;
import org.projectodd.restafari.stomp.common.StompMessageEncoder;
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
        //ch.pipeline().addLast( new DebugHandler( "server-head" ) );
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
        pipeline.addLast("http-resource-decoder", new HttpResourceRequestDecoder());
        pipeline.addLast("http-resource-encoder", new HttpResourceResponseEncoder());
        pipeline.addLast("http-error-encoder", new HttpErrorResponseEncoder());
        /*
        pipeline.addLast(new HttpGetCollectionRequestDecoder(this.container));
        pipeline.addLast(new HttpGetResourceRequestDecoder(this.container));
        pipeline.addLast(new HttpCreateResourceRequestDecoder(this.container));
        pipeline.addLast(new HttpUpdateResourceRequestDecoder(this.container));
        pipeline.addLast(new HttpDeleteResourceRequestDecoder(this.container));

        pipeline.addLast(new HttpResourceResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpResourcesResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpCreateResourceRequestEncoder(this.container));
        pipeline.addLast(new HttpUpdateResourceRequestEncoder(this.container));
        pipeline.addLast(new HttpDeleteResourceRequestEncoder(this.container));
        pipeline.addLast(new HttpNoSuchCollectionResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpNoSuchResourceResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpErrorResponseEncoder(this.container.getCodecManager()));
        */
        pipeline.addLast(new ContainerHandler(this.container));
    }

    private Container container;
}
