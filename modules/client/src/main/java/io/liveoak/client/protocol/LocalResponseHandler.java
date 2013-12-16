package io.liveoak.client.protocol;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import io.liveoak.client.ClientRequest;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.client.impl.ClientResourceResponseImpl;
import io.liveoak.common.codec.driver.RootEncodingDriver;
import io.liveoak.common.codec.state.ResourceStateEncoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class LocalResponseHandler extends ChannelDuplexHandler {

    public LocalResponseHandler() {
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientRequest) {
            ResourceRequest request = ((ClientRequest) msg).resourceRequest();
            Consumer<ClientResourceResponse> handler = ((ClientRequest) msg).handler();
            this.handlers.put(request, handler);
            super.write(ctx, request, promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ClientResourceResponseImpl) {
            ResourceRequest inReplyTo = ((ClientResourceResponseImpl) msg).inReplyTo();
            Consumer<ClientResourceResponse> handler = this.handlers.remove(inReplyTo);
            if (handler != null) {
                handler.accept((ClientResourceResponse) msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private Map<ResourceRequest, Consumer<ClientResourceResponse>> handlers = new ConcurrentHashMap<>();
}
