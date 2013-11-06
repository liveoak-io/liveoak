package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
        ResourceRequest request = new ResourceRequest.Builder(ResourceRequest.RequestType.CREATE, new ResourcePath(path))
                .resourceState(state)
                .build();
        this.handlers.put(request, handler);
        this.channel.writeInbound(request);
    }

    public Resource create(String path, ResourceState state) throws ExecutionException, InterruptedException {
        CompletableFuture<Resource> future = new CompletableFuture<>();

        create(path, state, (response) -> {
            if (response.responseType() == ResourceResponse.ResponseType.CREATED) {
                future.complete(response.resource());
            } else if (response instanceof ResourceErrorResponse) {
                handleError((ResourceErrorResponse) response, future);
            } else {
                future.complete(null);
            }
        });

        return future.get();
    }

    public void read(String path, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest.Builder(ResourceRequest.RequestType.READ, new ResourcePath(path))
                .build();
        this.handlers.put(request, handler);
        this.channel.writeInbound(request);
    }

    public Resource read(String path) throws ResourceException, ExecutionException, InterruptedException {
        CompletableFuture<Resource> future = new CompletableFuture<>();

        read(path, (response) -> {
            if (response.responseType() == ResourceResponse.ResponseType.READ) {
                future.complete(response.resource());
            } else if (response instanceof ResourceErrorResponse) {
                handleError((ResourceErrorResponse) response, future);
            } else {
                future.complete(null);
            }
        });

        try {
            return future.get();
        } catch (ExecutionException e) {
            if ( e.getCause() instanceof ResourceException ) {
                throw (ResourceException) e.getCause();
            }
            throw e;
        }
    }

    public void update(String path, ResourceState state, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest.Builder(ResourceRequest.RequestType.UPDATE, new ResourcePath(path))
                .resourceState(state)
                .build();
        this.handlers.put(request, handler);
        this.channel.writeInbound(request);
    }

    public Resource update(String path, ResourceState state) throws ExecutionException, InterruptedException {
        CompletableFuture<Resource> future = new CompletableFuture<>();

        update(path, state, (response) -> {
            if (response.responseType() == ResourceResponse.ResponseType.UPDATED) {
                future.complete(response.resource());
            } else if (response instanceof ResourceErrorResponse) {
                handleError((ResourceErrorResponse) response, future);
            } else {
                future.complete(null);
            }
        });

        return future.get();
    }

    public void delete(String path, Consumer<ResourceResponse> handler) {
        ResourceRequest request = new ResourceRequest.Builder(ResourceRequest.RequestType.DELETE, new ResourcePath(path))
                .build();
        this.handlers.put(request, handler);
        this.channel.writeInbound(request);
    }

    public Resource delete(String path) throws ExecutionException, InterruptedException {
        CompletableFuture<Resource> future = new CompletableFuture<>();

        delete(path, (response) -> {
            if (response.responseType() == ResourceResponse.ResponseType.UPDATED) {
                future.complete(response.resource());
            } else if (response instanceof ResourceErrorResponse) {
                handleError((ResourceErrorResponse) response, future);
            } else {
                future.complete(null);
            }
        });

        return future.get();
    }

    void handleError(ResourceErrorResponse response, CompletableFuture<Resource> future) {
        switch (((ResourceErrorResponse) response).errorType()) {
            case NOT_AUTHORIZED:
                future.completeExceptionally(new NotAuthorizedException(response.inReplyTo().resourcePath().toString()));
                break;
            case NOT_ACCEPTABLE:
                break;
            case NO_SUCH_RESOURCE:
                future.completeExceptionally(new ResourceNotFoundException(response.inReplyTo().resourcePath().toString()));
                break;
            case CREATE_NOT_SUPPORTED:
                future.completeExceptionally(new CreateNotSupportedException(response.inReplyTo().resourcePath().toString()));
                break;
            case READ_NOT_SUPPORTED:
                future.completeExceptionally(new ReadNotSupportedException(response.inReplyTo().resourcePath().toString()));
                break;
            case UPDATE_NOT_SUPPORTED:
                future.completeExceptionally(new UpdateNotSupportedException(response.inReplyTo().resourcePath().toString()));
                break;
            case DELETE_NOT_SUPPORTED:
                future.completeExceptionally(new DeleteNotSupportedException(response.inReplyTo().resourcePath().toString()));
                break;
        }
    }

    void dispatch(Object obj) {
        if (obj instanceof ResourceResponse) {
            Consumer<ResourceResponse> handler = this.handlers.remove(((ResourceResponse) obj).inReplyTo());
            if (handler != null) {
                handler.accept((ResourceResponse) obj);
            }
        }
    }

    private DefaultContainer container;
    private final EmbeddedChannel channel;
    private Map<ResourceRequest, Consumer<ResourceResponse>> handlers = new HashMap<>();

}
