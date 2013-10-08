package org.projectodd.restafari.container.protocols;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ContainerHandler;
import org.projectodd.restafari.container.protocols.http.*;
import org.projectodd.restafari.stomp.StompFrameDecoder;
import org.projectodd.restafari.stomp.StompFrameEncoder;
import org.projectodd.restafari.container.protocols.websocket.WebSocketHandshakerHandler;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameDecoder;
import org.projectodd.restafari.container.protocols.websocket.WebSocketStompFrameEncoder;

/**
 * @author Bob McWhirter
 */
public class PipelineConfigurator {

    public PipelineConfigurator(Container container) {
        this.container = container;
    }

    public void switchToPureStomp(ChannelPipeline pipeline) {
        pipeline.remove( ProtocolDetector.class );
        pipeline.addLast(new StompFrameDecoder());
        pipeline.addLast(new StompFrameEncoder());
    }

    public void switchToHttpWebSockets(ChannelPipeline pipeline) {
        pipeline.remove( ProtocolDetector.class );
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new WebSocketHandshakerHandler(this));
    }

    public void switchToWebSockets(ChannelPipeline pipeline) {
        pipeline.remove(WebSocketHandshakerHandler.class);
        pipeline.addLast(new WebSocketStompFrameDecoder());
        pipeline.addLast(new WebSocketStompFrameEncoder());
    }

    public void switchToPlainHttp(ChannelPipeline pipeline) {
        pipeline.remove(WebSocketHandshakerHandler.class);
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
        pipeline.addLast( new ContainerHandler( this.container ) );
    }

    private Container container;
}
