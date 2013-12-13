package io.liveoak.client.protocol;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.liveoak.client.ClientRequest;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.client.impl.ClientResourceResponseImpl;
import io.liveoak.common.codec.driver.RootEncodingDriver;
import io.liveoak.common.codec.state.ResourceStateEncoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class LocalResponseHandler extends ChannelDuplexHandler {
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
        if (msg instanceof ResourceResponse) {
            Consumer<ClientResourceResponse> handler = this.handlers.remove(((ResourceResponse) msg).inReplyTo());
            if (handler != null) {
                ClientResourceResponse.ResponseType responseType = null;
                if (msg instanceof ResourceErrorResponse) {
                    responseType = decodeResponseType(((ResourceErrorResponse) msg).errorType());
                } else {
                    responseType = ClientResourceResponse.ResponseType.OK;
                }
                ClientResourceResponse response = null;
                try {
                    response = new ClientResourceResponseImpl(
                            responseType,
                            ((ResourceResponse) msg).inReplyTo().resourcePath().toString(),
                            encode(((ResourceResponse) msg).inReplyTo().requestContext(), ((ResourceResponse) msg).resource()));
                } catch (Exception e) {
                    response = new ClientResourceResponseImpl(
                            ClientResourceResponse.ResponseType.NOT_ACCEPTABLE,
                            ((ResourceResponse) msg).inReplyTo().resourcePath().toString(),
                            null);
                }
                handler.accept(response);
            }
        } else {
            super.channelRead(ctx, msg);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    /**
     * Encode (for some cheap value of 'encode') a resulting resource into a ResourceState.
     *
     * @param context  The request context (for expansion/fields).
     * @param resource The resource to encode.
     * @return The encoded resource state.
     * @throws Exception
     */
    protected ResourceState encode(RequestContext context, Resource resource) throws Exception {
        if (resource == null) {
            return null;
        }
        final CompletableFuture<ResourceState> state = new CompletableFuture<>();

        final ResourceStateEncoder encoder = new ResourceStateEncoder();
        RootEncodingDriver driver = new RootEncodingDriver(context, encoder, resource, () -> {
            state.complete(encoder.root());
        });

        driver.encode();

        return state.get();
    }

    private Map<ResourceRequest, Consumer<ClientResourceResponse>> handlers = new ConcurrentHashMap<>();
}
