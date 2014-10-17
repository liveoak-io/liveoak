package io.liveoak.client.protocol;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import io.liveoak.client.ClientRequest;
import io.liveoak.client.impl.ClientResourceResponseImpl;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.client.ClientResourceResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class LocalResponseHandler extends ChannelDuplexHandler {

    public LocalResponseHandler(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientRequest) {
            ResourceRequest request = ((ClientRequest) msg).resourceRequest();
            Consumer<ClientResourceResponse> handler = ((ClientRequest) msg).handler();
            this.handlers.put(request.requestId(), handler);
            super.write(ctx, request, promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ClientResourceResponseImpl) {
            ResourceRequest inReplyTo = ((ClientResourceResponseImpl) msg).inReplyTo();
            Consumer<ClientResourceResponse> handler = this.handlers.remove(inReplyTo.requestId());
            if (handler != null) {
                this.executor.execute(() -> handler.accept((ClientResourceResponse) msg));
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private Map<UUID, Consumer<ClientResourceResponse>> handlers = new ConcurrentHashMap<>();
    private ExecutorService executor;
}
