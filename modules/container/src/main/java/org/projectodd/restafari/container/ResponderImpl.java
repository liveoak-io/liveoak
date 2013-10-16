package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.requests.HttpResourceRequest;
import org.projectodd.restafari.container.responses.HttpErrors;
import org.projectodd.restafari.container.responses.DefaultHttpResourceResponse;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class ResponderImpl implements Responder {

    public ResponderImpl(Container container, HttpResourceRequest request, ChannelHandlerContext ctx) {
        this.container = container;
        this.request = request;
        this.ctx = ctx;
    }

    @Override
    public void resource(Resource resource) {
        writeResponse(resource, null);
    }

    @Override
    public void resources(Collection<Resource> resources) {
        writeResponse(resources);
    }

    @Override
    public void resourceCreated(Resource resource) {
        writeResponse(resource, (sub) -> {
            sub.resourceCreated(request.getResourceType(), request.getResourcePath().getCollectionName(), resource);
        });
    }

    @Override
    public void resourceUpdated(Resource resource) {
        writeResponse(resource, (sub) -> {
            sub.resourceUpdated(request.getResourceType(), request.getResourcePath().getCollectionName(), resource);
        });
    }

    @Override
    public void resourceDeleted(Resource resource) {
        //TODO: Do we want to return the resource that was deleted ?
        writeResponse(null, (sub) -> {
            sub.resourceDeleted(request.getResourceType(), request.getResourcePath().getCollectionName(), resource);
        });
    }

    @Override
    public void noSuchCollection(String name) {
        this.ctx.writeAndFlush(HttpErrors.notFound(request.getUri()));
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush(HttpErrors.notFound(request.getUri()));
    }

    @Override
    public void internalError(String message) {
        this.ctx.writeAndFlush(HttpErrors.internalError(message));
    }

    @Override
    public void collectionDeleted(String name) {
        writeResponse(null, (sub) -> {
            sub.collectionDeleted(request.getResourceType(), request.getResourcePath().getCollectionName());
        });
    }

    private void writeResponse(Resource resource, Consumer<SubscriptionManager> subMgrConsumer) {
        try {
            String contentType = getContentType(request);
            ResourceCodec codec = container.getCodecManager().getResourceCodec(contentType);
            if (codec == null) {
                //TODO: Get list of acceptable content types
                ctx.writeAndFlush(HttpErrors.notAcceptable(request.getMimeType()));
            } else {
                // Write the response with content encoded
                ctx.writeAndFlush(new DefaultHttpResourceResponse(request, codec.encode(resource), contentType));
                // Notify subscribers
                if (subMgrConsumer != null) {
                    subMgrConsumer.accept(container.getSubscriptionManager());
                }
            }
        } catch (IOException e) {
            ctx.writeAndFlush(HttpErrors.internalError(e.getMessage()));
        }
    }

    private void writeResponse(Collection<Resource> resources) {
        try {
            String contentType = getContentType(request);
            ResourceCodec codec = container.getCodecManager().getResourceCodec(contentType);
            if (codec == null) {
                //TODO: Get list of acceptable content types
                ctx.writeAndFlush(HttpErrors.notAcceptable(request.getMimeType()));
            } else {
                ctx.writeAndFlush(new DefaultHttpResourceResponse(request, codec.encode(resources), contentType));
            }
        } catch (IOException e) {
            ctx.writeAndFlush(HttpErrors.internalError(e.getMessage()));
        }
    }

    private static String getContentType(HttpResourceRequest request) {
        //TODO: Need to properly parse this, as this can have multiple values i.e. "text/plain; q=0.5, text/html"
        String accept = request.headers().get(HttpHeaders.Names.ACCEPT);
        if (accept == null || "*/*".equals(accept)) {
            accept = request.getMimeType(); // Use mime type of request
        }
        return accept;
    }

    private Container container;
    private HttpResourceRequest request;
    private ChannelHandlerContext ctx;

}
