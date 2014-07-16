package io.liveoak.container.protocols.local;

import java.util.concurrent.Executor;

import io.liveoak.client.impl.ClientResourceResponseImpl;
import io.liveoak.container.protocols.RequestCompleteEvent;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.ClientResourceResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class LocalResourceResponseEncoder extends ChannelOutboundHandlerAdapter {

    public LocalResourceResponseEncoder(Executor workerPool) {
        this.workerPool = workerPool;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ResourceResponse) {
            ResourceResponse response = (ResourceResponse) msg;
            if (msg instanceof ResourceErrorResponse) {
                ClientResourceResponse.ResponseType responseType = decodeResponseType(((ResourceErrorResponse) msg).errorType());
                ctx.writeAndFlush(new ClientResourceResponseImpl(response.inReplyTo(), responseType, response.inReplyTo().resourcePath().toString(), null));
            } else {
                encode(ctx, response);
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }

    private ClientResourceResponse.ResponseType decodeResponseType(ResourceErrorResponse.ErrorType errorType) {
        switch (errorType) {
            case NOT_AUTHORIZED:
                return ClientResourceResponse.ResponseType.NOT_AUTHORIZED;
            case NOT_ACCEPTABLE:
                return ClientResourceResponse.ResponseType.NOT_ACCEPTABLE;
            case NO_SUCH_RESOURCE:
                return ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE;
            case RESOURCE_ALREADY_EXISTS:
                return ClientResourceResponse.ResponseType.RESOURCE_ALREADY_EXISTS;
            case CREATE_NOT_SUPPORTED:
                return ClientResourceResponse.ResponseType.CREATE_NOT_SUPPORTED;
            case READ_NOT_SUPPORTED:
                return ClientResourceResponse.ResponseType.READ_NOT_SUPPORTED;
            case UPDATE_NOT_SUPPORTED:
                return ClientResourceResponse.ResponseType.UPDATE_NOT_SUPPORTED;
            case DELETE_NOT_SUPPORTED:
                return ClientResourceResponse.ResponseType.DELETE_NOT_SUPPORTED;
            case INTERNAL_ERROR:
                return ClientResourceResponse.ResponseType.INTERNAL_ERROR;
        }

        return ClientResourceResponse.ResponseType.ERROR;
    }

    /**
     * Encode (for some cheap value of 'encode') a resulting resource into a ResourceState.
     *
     * @param ctx
     * @param response The response to encode.
     * @throws Exception
     */
    protected void encode(ChannelHandlerContext ctx, ResourceResponse response) {
        final ClientResourceResponse.ResponseType responseType = ClientResourceResponse.ResponseType.OK;
        if (response.resource() == null) {
            ctx.writeAndFlush(new ClientResourceResponseImpl(response.inReplyTo(), responseType, response.inReplyTo().resourcePath().toString(), null));
            ctx.fireUserEventTriggered(new RequestCompleteEvent(response.requestId()));
            return;
        }

        ctx.writeAndFlush(new ClientResourceResponseImpl(response.inReplyTo(), responseType, response.inReplyTo().resourcePath().toString(), response.state()));
        ctx.fireUserEventTriggered(new RequestCompleteEvent(response.requestId()));

    }

    private Executor workerPool;
}
