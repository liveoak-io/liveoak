/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.aspects.ResourceAspectManager;
import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.container.codec.html.HTMLEncoder;
import io.liveoak.container.codec.json.JSONDecoder;
import io.liveoak.container.codec.json.JSONEncoder;
import io.liveoak.container.subscriptions.SubscriptionManager;
import io.liveoak.spi.Config;
import io.liveoak.spi.Container;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformLocator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DefaultContainer implements Container, Resource {

    public DefaultContainer() {
        this( PlatformLocator.factory.createPlatformManager().vertx() );
    }

    public DefaultContainer( Vertx vertx ) {
        this.codecManager.registerResourceCodec( "application/json", new ResourceCodec( this, JSONEncoder.class, new JSONDecoder() ) );
        this.codecManager.registerResourceCodec( "text/html", new ResourceCodec( this, HTMLEncoder.class, null ) );

        this.vertx = vertx;
        this.workerPool = Executors.newCachedThreadPool();

        this.subscriptionManager = new SubscriptionManager( "subscriptions", this.codecManager );
        try {
            registerResource( this.subscriptionManager, new SimpleConfig() );
        } catch (InitializationException e) {
            // ignore
        }

        //this.aspectManager.put( "_subscriptions", new SubscriptionsResourceAspect( this.subscriptionManager ) );
    }

    @Override
    public void shutdown() {
        this.resources.values().forEach( ( res ) -> {
            res.destroy();
        } );

        this.resources.clear();
    }

    public void registerResource( RootResource resource, Config config ) throws InitializationException {
        //TODO: Lazy initialization in holder class when resourceRead controller is first accessed
        resource.initialize( new SimpleResourceContext( this.vertx, this, config ) );
        this.resources.put( resource.id(), resource );
    }

    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    SubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    Executor workerPool() {
        return this.workerPool;
    }

    public ResourceAspectManager resourceAspectManager() {
        return this.aspectManager;
    }

    public DirectConnector directConnector() {
        return new DirectConnector( this );
    }

    // ----------------------------------------
    // CollectionResource
    // ----------------------------------------

    @Override
    public void readMember( RequestContext ctx, String id, Responder responder ) {
        try {
            if ( id == null ) {
                responder.resourceRead( this );
                return;
            }

            if ( !this.resources.containsKey( id ) ) {
                responder.noSuchResource( id );
                return;
            }

            responder.resourceRead( this.resources.get( id ) );

        } catch ( Throwable t ) {
            responder.internalError( t.getMessage() );
        }
    }

    @Override
    public void readMembers( RequestContext ctx, ResourceSink sink ) {
        this.resources.values().forEach( ( e ) -> {
            sink.accept( e );
        } );
        try {
            sink.close();
        } catch ( Exception e ) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return this.prefix;
    }

    private ResourceAspectManager aspectManager = new ResourceAspectManager();
    private String prefix = "";
    private Map<String, RootResource> resources = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();
    private Vertx vertx;
    private SubscriptionManager subscriptionManager;
    private Executor workerPool;
}


