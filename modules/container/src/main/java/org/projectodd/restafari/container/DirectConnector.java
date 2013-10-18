package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class DirectConnector {

    private class DirectCallbackHandler extends ChannelOutboundHandlerAdapter {
        public DirectCallbackHandler() {
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            promise.addListener((f) -> {
                Object o = DirectConnector.this.channel.readOutbound();
                DirectConnector.this.dispatch(o);
            });
            super.write(ctx, msg, promise);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


    public DirectConnector(DefaultContainer container) {
        this.container = container;
        this.channel = new EmbeddedChannel(new DirectCallbackHandler(), new ResourceHandler(this.container));
        this.channel.readInbound();
    }

    public void create(String path, ResourceState state, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest(ResourceRequest.RequestType.CREATE, new ResourcePath(path), "ignored", state );
        this.handlers.put( request, handler );
        this.channel.writeInbound(request);
    }

    public void read(String path, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest(ResourceRequest.RequestType.READ, new ResourcePath(path), "ignored");
        this.handlers.put( request, handler );
        this.channel.writeInbound(request);
    }

    public void update(String path, ResourceState state, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest(ResourceRequest.RequestType.UPDATE, new ResourcePath(path), "ignored", state );
        this.handlers.put( request, handler );
        this.channel.writeInbound(request);
    }

    public void delete(String path, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest(ResourceRequest.RequestType.DELETE, new ResourcePath(path), "ignored");
        this.handlers.put( request, handler );
        this.channel.writeInbound(request);
    }

    void dispatch(Object obj) {
        if ( obj instanceof ResourceResponse ) {
            Consumer<ResourceResponse> handler = this.handlers.remove(((ResourceResponse) obj).inReplyTo());
            if ( handler != null ) {
                handler.accept((ResourceResponse) obj);
            }
        }
    }

    private DefaultContainer container;
    private final EmbeddedChannel channel;
    private Map<ResourceRequest, Consumer<ResourceResponse>> handlers = new HashMap<>();

}
