package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.requests.CollectionRequest;
import org.projectodd.restafari.container.requests.ResourceRequest;
import org.projectodd.restafari.spi.Resource;

import java.util.List;

/**
 * //TODO: Can't rely on FullHttpRequest if we are to support http chunks
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpResourceRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    public static final String DEFAULT_MIME_TYPE = "application/json";

    public HttpResourceRequestDecoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        ResourcePath resourcePath = new ResourcePath(msg.getUri());

        String mimeType = msg.headers().get(HttpHeaders.Names.ACCEPT);
        if (mimeType == null) {
            mimeType = DEFAULT_MIME_TYPE;
        }

        if (resourcePath.isCollectionPath()) {
            if (msg.getMethod().equals(HttpMethod.GET)) {
                out.add(new CollectionRequest(CollectionRequest.RequestType.READ, resourcePath, mimeType, null));
            } else if (msg.getMethod().equals(HttpMethod.POST)) {
                ResourceCodec codec = codecManager.getResourceCodec(mimeType);
                Resource resource = (Resource) codec.decode( msg.content() );
                out.add(new ResourceRequest(ResourceRequest.RequestType.CREATE, resourcePath, mimeType, resource));
            } else {
                // TODO throw an error to the HTTP level directly, since we know we're on HTTP at the moment.
                return;
            }
        } else if (resourcePath.isResourcePath()) {
            ResourceRequest.RequestType requestType = null;
            Resource resource = null;
            if (msg.getMethod() == HttpMethod.GET) {
                requestType = ResourceRequest.RequestType.READ;
            } else if (msg.getMethod() == HttpMethod.PUT) {
                requestType = ResourceRequest.RequestType.UPDATE;
                ResourceCodec codec = codecManager.getResourceCodec(mimeType);
                resource = (Resource) codec.decode( msg.content() );
            } else if (msg.getMethod().equals(HttpMethod.DELETE)) {
                requestType = ResourceRequest.RequestType.DELETE;
            } else {
                // TODO throw an error to the HTTP level directly, since we know we're on HTTP at the moment.
                return;
            }
            out.add( new ResourceRequest( requestType, resourcePath, mimeType, resource ) );
        }
    }

    private ResourceCodecManager codecManager;
}
