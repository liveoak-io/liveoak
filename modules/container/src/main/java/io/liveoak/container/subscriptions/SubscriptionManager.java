/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions;

import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.spi.*;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class SubscriptionManager implements RootResource {

    public SubscriptionManager(String id, ResourceCodecManager codecManager) {
        this.id = id;
        this.codecManager = codecManager;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        this.vertx = context.vertx();
    }

    @Override
    public String id() {
        return this.id;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        String path = ctx.resourceParams().value("path");

        Stream<Subscription> subscriptions = null;

        if (path == null) {
            subscriptions = getSubscriptions();
        } else {
            subscriptions = getSubscriptions(new ResourcePath(path));
        }

        subscriptions.forEach((e) -> {
            sink.accept(e);
        });
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        Subscription subscription = getSubscription(id);
        if (subscription == null) {
            responder.noSuchResource(id);
        } else {
            responder.resourceRead(subscription);
        }
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

            HttpSubscription sub = new HttpSubscription(this, httpClient, path, destinationUri, codec);
            addSubscription(sub);
            responder.resourceCreated(sub);
        } catch (URISyntaxException e) {
            responder.internalError(e.getMessage());
        }
    }


    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    public void resourceCreated(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceCreated(resource);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    public void resourceUpdated(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceUpdated(resource);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    public void resourceDeleted(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceDeleted(resource);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    public Stream<Subscription> getSubscriptions(Resource resource) {
        ResourcePath resourcePath = resourcePathOf(resource);
        return getSubscriptions(resourcePath);
    }

    public Stream<Subscription> getSubscriptions(ResourcePath resourcePath) {
        return this.subscriptions.stream().filter((subscription) -> {
            ResourcePath subscriptionPath = subscription.resourcePath();
            return matches(subscriptionPath, resourcePath);
        });
    }

    public Stream<Subscription> getSubscriptions() {
        return this.subscriptions.stream();
    }

    public Subscription getSubscription(String id) {
        Optional<Subscription> found = this.subscriptions.stream().filter(e -> e.id().equals(id)).findFirst();
        if (found.isPresent()) {
            return found.get();
        }
        return null;
    }

    protected boolean matches(ResourcePath subscriptionPath, ResourcePath resourcePath) {
        List<ResourcePath.Segment> subscriptionSegments = subscriptionPath.segments();
        List<ResourcePath.Segment> resourceSegments = resourcePath.segments();

        if (subscriptionSegments.size() > resourceSegments.size()) {
            return false;
        }

        int numSegments = subscriptionSegments.size();

        for (int i = 0; i < numSegments; ++i) {
            ResourcePath.Segment subscriptionSegment = subscriptionSegments.get(i);
            if (subscriptionSegment.name().equals("*")) {
                continue;
            }
            ResourcePath.Segment resourceSegment = resourceSegments.get(i);

            if (!subscriptionSegment.equals(resourceSegment)) {
                return false;
            }
        }

        return true;
    }

    protected ResourcePath resourcePathOf(Resource resource) {
        ResourcePath path = new ResourcePath();

        Resource current = resource;

        while (current != null) {
            path.prependSegment(current.id());
            current = current.parent();
        }

        return path;
    }

    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        this.subscriptions.remove(subscription);
    }

    private String id;
    private ResourceCodecManager codecManager;
    private Vertx vertx;
    private List<Subscription> subscriptions = new ArrayList<>();


}
