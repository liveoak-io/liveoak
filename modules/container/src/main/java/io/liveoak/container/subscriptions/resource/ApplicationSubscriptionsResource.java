package io.liveoak.container.subscriptions.resource;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.container.subscriptions.HttpSubscription;
import io.liveoak.container.subscriptions.StompSubscription;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class ApplicationSubscriptionsResource implements SynchronousResource, RootResource {

    public ApplicationSubscriptionsResource(DefaultSubscriptionManager subscriptionManager, Vertx vertx, ResourceCodecManager codecManager) {
        this.subscriptionManager = subscriptionManager;
        this.vertx = vertx;
        this.codecManager = codecManager;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "subscriptions";
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {

        String path = (String) state.getProperty("path");
        String destination = (String) state.getProperty("destination");
        String contentType = (String) state.getProperty("content-type");

        if (contentType == null) {
            contentType = "application/json";
        }

        ResourceCodec codec = this.codecManager.getResourceCodec(new MediaType(contentType));

        if (codec == null) {
            responder.internalError("content-type not supported: " + contentType);
            return;
        }


        try {
            URI destinationUri = new URI(destination);
            HttpClient httpClient = this.vertx.createHttpClient();
            httpClient.setHost(destinationUri.getHost());
            httpClient.setPort(destinationUri.getPort());

            SecurityContext requestSecurityContext = ctx.securityContext();
            HttpSubscription sub = new HttpSubscription(httpClient, path, destinationUri, codec, requestSecurityContext);
            this.subscriptionManager.addSubscription(sub);
            responder.resourceCreated(new HttpSubscriptionResource(this, sub));
        } catch (URISyntaxException e) {
            responder.internalError(e.getMessage());
        }
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) {
        Stream<Subscription> subscriptionStream = subscriptionManager.treeFor(applicationResourcePath()).objects();
        return subscriptionStream.map((e) -> {
            if (e instanceof StompSubscription) {
                return new StompSubscriptionResource(this, (StompSubscription) e);
            } else if (e instanceof HttpSubscription) {
                return new HttpSubscriptionResource(this, (HttpSubscription) e);
            } else if (e instanceof Resource) {
                return (Resource) e;
            } else {
                return new BasicSubscriptionResource(this, e);
            }
        }).collect( Collectors.toList() );
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        Optional<Subscription> result = subscriptionManager.treeFor(applicationResourcePath())
                .objects()
                .filter(e -> e.id().equals(id))
                .findFirst();

        if ( result.isPresent() ) {
            Subscription subscription = result.get();
            if ( subscription instanceof HttpSubscription ) {
                return new HttpSubscriptionResource( this, (HttpSubscription) subscription);
            } else if ( subscription instanceof StompSubscription ) {
                return new StompSubscriptionResource( this, (StompSubscription) subscription);
            } else if (subscription instanceof Resource) {
                return (Resource) subscription;
            }  else {
                return new BasicSubscriptionResource( this, subscription);
            }
        }

        return null;
    }

    protected ResourcePath applicationResourcePath() {
        ResourcePath path = new ResourcePath();
        path.prependSegment(this.parent().id());
        return path;
    }

    public void delete(Subscription subscription) {
        this.subscriptionManager.removeSubscription( subscription );
    }


    private Resource parent;
    private DefaultSubscriptionManager subscriptionManager;
    private Vertx vertx;
    private ResourceCodecManager codecManager;
}
