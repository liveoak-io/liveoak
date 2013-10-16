package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.requests.HttpResourceRequest;
import org.projectodd.restafari.container.responses.HttpErrors;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

import java.io.IOException;
import java.util.function.Consumer;

public class ContainerHandler extends ChannelDuplexHandler {

    public ContainerHandler(Container container) {
        this.container = container;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResourceRequest) {
            HttpResourceRequest request = (HttpResourceRequest) msg;
            Holder holder = container.getResourceController(request.getResourceType());
            if (holder == null) {
                ctx.writeAndFlush(HttpErrors.notFound(request.getUri()));
                return;
            }

            final ResourceController controller = holder.getResourceController();
            final Responder responder = new ResponderImpl(container, request, ctx);
            final String collectionName = request.getResourcePath().getCollectionName();
            final String resourceId = request.getResourcePath().getResourceId();
            if (request.isResourceRequest()) {
                switch (request.getHttpMethod()) {
                    case "GET":
                        controller.getResource(null, collectionName, resourceId, responder);
                        break;
                    case "PUT":
                        decode(ctx, request, resource -> {
                            if (resource == null) {
                                ctx.writeAndFlush(HttpErrors.badRequest("No content"));
                            } else {
                                controller.updateResource(null, collectionName, resourceId, resource, responder);
                            }
                        });
                        break;
                    case "DELETE":
                        controller.deleteResource(null, collectionName, resourceId, responder);
                        break;
                    default:
                        ctx.writeAndFlush(HttpErrors.methodNotAllowed(resourceMethods));
                }
            } else if (request.isCollectionRequest()) {
                switch (request.getHttpMethod()) {
                    case "GET":
                        controller.getResources(null, collectionName, request.getPagination(), responder);
                        break;
                    case "POST":
                        decode(ctx, request, resource -> {
                            controller.createResource(null, collectionName, resource, responder);
                        });
                        break;
                    case "DELETE":
                        controller.deleteCollection(null, collectionName, responder);
                    default:
                        ctx.writeAndFlush(HttpErrors.methodNotAllowed(collectionMethods));
                }
            } else if (request.isTypeRequest()) {
                switch (request.getHttpMethod()) {
                    case "GET":
                        controller.getCollections(null, request.getPagination(), responder );
                        break;
                }
            } else {
                ctx.writeAndFlush(HttpErrors.notFound(request.getUri()));
            }
        } else {
            ctx.fireExceptionCaught(new Exception("Unknown message type " + msg));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(System.err);
        ctx.pipeline().write(HttpErrors.internalError(cause.getMessage()));
        ctx.pipeline().flush();
    }

    private void decode(ChannelHandlerContext ctx, HttpResourceRequest request, Consumer<Resource> consumer) throws IOException {
        ResourceCodec codec = container.getCodecManager().getResourceCodec(request.getMimeType());
        if (codec == null) {
            ctx.writeAndFlush(HttpErrors.unsupportedMediaType());
        } else {
            try {
                Resource resource = (Resource) codec.decode(request.content());
                consumer.accept(resource);
            } catch (IOException e) {
                ctx.writeAndFlush(HttpErrors.badRequest(e.getMessage()));
            }
        }
    }

    private Container container;

    private static final String[] resourceMethods = new String[]{HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name()};
    private static final String[] collectionMethods = new String[]{HttpMethod.GET.name(), HttpMethod.POST.name()};
}
