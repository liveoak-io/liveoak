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
        this.executor = Executors.newCachedThreadPool();
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
        if (msg instanceof ResourceResponse) {
            Consumer<ClientResourceResponse> handler = this.handlers.remove(((ResourceResponse) msg).inReplyTo());
            if (handler != null) {
                Runnable action = ()->{
                    ClientResourceResponse.ResponseType responseType = null;
                    if (msg instanceof ResourceErrorResponse) {
                        responseType = decodeResponseType(((ResourceErrorResponse) msg).errorType());
                    } else {
                        responseType = ClientResourceResponse.ResponseType.OK;
                    }
                    encode(responseType, ((ResourceResponse) msg).inReplyTo().requestContext(), ((ResourceResponse)msg).inReplyTo().resourcePath(),((ResourceResponse) msg).resource(), handler);
                };
                if ( ((ResourceResponse) msg).resource() instanceof BlockingResource ) {
                    this.executor.execute( action );
                } else {
                    action.run();
                }
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
     *
     * @param responseType
     * @param context      The request context (for expansion/fields).
     * @param resourcePath
     *@param resource     The resource to encode.  @return The encoded resource state.
     * @throws Exception
     */
    protected void encode(ClientResourceResponse.ResponseType responseType, RequestContext context, ResourcePath resourcePath, Resource resource, Consumer<ClientResourceResponse> handler) {
        if (resource == null) {
            ClientResourceResponse response = new ClientResourceResponseImpl(responseType, resourcePath.toString(), null);
            handler.accept(response);
            return;
        }

        final ResourceStateEncoder encoder = new ResourceStateEncoder();
        RootEncodingDriver driver = new RootEncodingDriver(context, encoder, resource, () -> {
            ResourceState state = encoder.root();
            ClientResourceResponse response = new ClientResourceResponseImpl(responseType, resourcePath.toString(), state);
            handler.accept(response);
        });

        try {
            driver.encode();
        } catch (Exception e) {
            ClientResourceResponse response = new ClientResourceResponseImpl(
                    ClientResourceResponse.ResponseType.NOT_ACCEPTABLE,
                    resourcePath.toString(),
                    null);
            handler.accept( response );
        }
    }

    private Executor executor;
    private Map<ResourceRequest, Consumer<ClientResourceResponse>> handlers = new ConcurrentHashMap<>();
}
