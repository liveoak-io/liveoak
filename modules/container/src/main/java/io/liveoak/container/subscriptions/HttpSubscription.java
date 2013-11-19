package io.liveoak.container.subscriptions;

import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
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

    public HttpSubscription(SubscriptionManager subscriptionManager, HttpClient httpClient, String path, URI destination, ResourceCodec codec) {
        this.subscriptionManager = subscriptionManager;
        this.id = UUID.randomUUID().toString();
        this.httpClient = httpClient;
        this.resourcePath = new ResourcePath(path);
        this.destination = destination;
        this.codec = codec;
    }

    @Override
    public Resource parent() {
        return this.subscriptionManager;
    }

    @Override
    public String id() {
        return this.id;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        sink.accept("type", "http");
        sink.accept("path", this.resourcePath.toString());
        sink.accept("destination", this.destination.toString());
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        this.subscriptionManager.removeSubscription( this );
        responder.resourceDeleted( this );
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    @Override
    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    @Override
    public void resourceCreated(Resource resource) throws Exception {
        resourceUpdated(resource);
    }

    @Override
    public void resourceUpdated(Resource resource) throws Exception {
        HttpClientRequest request = this.httpClient.put(destinationUri(resource).toString(), (response) -> {
        });

        RequestContext requestContext = new RequestContext.Builder().build();
        ByteBuf encoded = codec.encode(requestContext, resource);
        request.write(new Buffer(encoded));
        request.end();
    }

    @Override
    public void resourceDeleted(Resource resource) throws Exception {
        HttpClientRequest request = this.httpClient.delete(destinationUri(resource).toString(), (response) -> {
        });

        RequestContext requestContext = new RequestContext.Builder().build();
        ByteBuf encoded = codec.encode(requestContext, resource);
        request.write(new Buffer(encoded));
        request.end();
    }

    protected URI destinationUri(Resource resource) {
        return this.destination.resolve(resource.id());
    }

    private SubscriptionManager subscriptionManager;
    private String id;
    private HttpClient httpClient;
    private ResourcePath resourcePath;
    private final URI destination;
    private ResourceCodec codec;

}
