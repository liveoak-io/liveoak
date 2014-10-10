package io.liveoak.container.subscriptions;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;

import java.net.URI;
import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class HttpSubscription implements Subscription {

    public HttpSubscription(HttpClient httpClient, String path, URI destination, ResourceCodec codec, SecurityContext securityContext) {
        this.id = UUID.randomUUID().toString();
        this.httpClient = httpClient;
        this.resourcePath = new ResourcePath(path);
        this.destination = destination;
        this.codec = codec;
        this.securityContext = securityContext;
    }

    @Override
    public String id() {
        return this.id;
    }

    public URI destination() {
        return this.destination;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    @Override
    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public SecurityContext securityContext() {
        return this.securityContext;
    }

    @Override
    public void sendAuthzError(ResourceState errorState, Resource resource, int status) throws Exception {
        URI uri = this.destination.resolve(resource.id());
        HttpClientRequest request = this.httpClient.put(uri.getPath(), (response) -> {
        });

        request.setChunked(true);

        RequestContext requestContext = new RequestContext.Builder().build();
        ByteBuf encoded = codec.encode(requestContext, errorState);
        request.write(new Buffer(encoded));
        request.end();
    }

    @Override
    public void resourceCreated(ResourceResponse resourceResponse) throws Exception {
        resourceUpdated(resourceResponse);
    }

    @Override
    public void resourceUpdated(ResourceResponse resourceResponse) throws Exception {
        URI uri = destinationUri(resourceResponse.resource());
        HttpClientRequest request = this.httpClient.put(uri.getPath(), (response) -> {
        });

        request.setChunked(true);

        RequestContext requestContext = new RequestContext.Builder().build();
        ByteBuf encoded = codec.encode(requestContext, resourceResponse.state());
        request.write(new Buffer(encoded));
        request.end();
    }

    @Override
    public void resourceDeleted(ResourceResponse resourceResponse) throws Exception {
        URI uri = destinationUri(resourceResponse.resource());
        HttpClientRequest request = this.httpClient.delete(uri.getPath(), (response) -> {
        });

        request.setChunked(true);

        RequestContext requestContext = new RequestContext.Builder().build();
        ByteBuf encoded = codec.encode(requestContext, resourceResponse.state());
        request.write(new Buffer(encoded));
        request.end();
    }

    protected URI destinationUri(Resource resource) {
        URI uri = this.destination.resolve(resource.id());
        return uri;
    }

    private String id;
    private HttpClient httpClient;
    private ResourcePath resourcePath;
    private final URI destination;
    private ResourceCodec codec;
    private final SecurityContext securityContext;

}
