/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.codec.driver.RootEncodingDriver;
import io.liveoak.container.codec.state.ResourceStateEncoder;
import io.liveoak.container.subscriptions.SubscriptionWatcher;
import io.liveoak.spi.CreateNotSupportedException;
import io.liveoak.spi.DeleteNotSupportedException;
import io.liveoak.spi.NotAuthorizedException;
import io.liveoak.spi.ReadNotSupportedException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceAlreadyExistsException;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.UpdateNotSupportedException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;

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
        public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) throws Exception {
            promise.addListener( ( f ) -> {
                Object o = DirectConnector.this.channel.readOutbound();
                DirectConnector.this.dispatch( o );
            } );
            super.write( ctx, msg, promise );    //To change body of overridden methods use File | Settings | File Templates.
        }
    }


    public DirectConnector( DefaultContainer container ) {
        this.container = container;
        this.channel = new EmbeddedChannel(
                new DirectCallbackHandler(),
                new SubscriptionWatcher( this.container.getSubscriptionManager() ),
                new ResourceHandler( this.container )
        );
        this.channel.readInbound();
    }

    public Resource fetch( String path ) throws ResourceException, ExecutionException, InterruptedException {
        CompletableFuture<Resource> future = new CompletableFuture<>();
        RequestContext context = new RequestContext.Builder().build();

        read( context, path, ( response ) -> {
            if ( response.responseType() == ResourceResponse.ResponseType.READ ) {
                try {
                    future.complete( response.resource() );
                } catch ( Exception e ) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if ( response instanceof ResourceErrorResponse ) {
                handleError( ( ResourceErrorResponse ) response, future );
            } else {
                future.complete( null );
            }
        } );

        try {
            return future.get();
        } catch ( ExecutionException e ) {
            if ( e.getCause() instanceof ResourceException ) {
                throw ( ResourceException ) e.getCause();
            }
            throw e;
        }
    }

    public void create( RequestContext context, String path, ResourceState state, Consumer<ResourceResponse> handler ) {
        ResourceRequest request = new ResourceRequest.Builder( RequestType.CREATE, new ResourcePath( path ) )
                .requestContext( context )
                .resourceState( state )
                .build();
        this.handlers.put( request, handler );
        this.channel.writeInbound( request );
    }

    public ResourceState create( RequestContext context, String path, ResourceState state ) throws ResourceException, ExecutionException, InterruptedException {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        create( context, path, state, ( response ) -> {
            if ( response.responseType() == ResourceResponse.ResponseType.CREATED ) {
                try {
                    future.complete(encode(context, response.resource()));
                } catch ( Exception e ) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if ( response instanceof ResourceErrorResponse ) {
                handleError( ( ResourceErrorResponse ) response, future );
            } else {
                future.complete( null );
            }
        } );

        try {
            return future.get();
        } catch ( ExecutionException e ) {
            if ( e.getCause() instanceof ResourceException ) {
                throw ( ResourceException ) e.getCause();
            }
            throw e;
        }
    }

    public void read( RequestContext context, String path, Consumer<ResourceResponse> handler ) {
        ResourceRequest request = new ResourceRequest.Builder( RequestType.READ, new ResourcePath( path ) )
                .requestContext( context )
                .build();
        this.handlers.put( request, handler );
        this.channel.writeInbound( request );
    }

    public ResourceState read( RequestContext context, String path ) throws ResourceException, ExecutionException, InterruptedException {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        read( context, path, ( response ) -> {
            if ( response.responseType() == ResourceResponse.ResponseType.READ ) {
                try {
                    future.complete( encode( context, response.resource() ) );
                } catch ( Exception e ) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if ( response instanceof ResourceErrorResponse ) {
                handleError( ( ResourceErrorResponse ) response, future );
            } else {
                future.complete( null );
            }
        } );

        try {
            return future.get();
        } catch ( ExecutionException e ) {
            if ( e.getCause() instanceof ResourceException ) {
                throw ( ResourceException ) e.getCause();
            }
            throw e;
        }
    }


    public void update( RequestContext context, String path, ResourceState state, Consumer<ResourceResponse> handler ) {
        ResourceRequest request = new ResourceRequest.Builder( RequestType.UPDATE, new ResourcePath( path ) )
                .requestContext( context )
                .resourceState( state )
                .build();
        this.handlers.put( request, handler );
        this.channel.writeInbound( request );
    }

    public ResourceState update( RequestContext context, String path, ResourceState state ) throws ExecutionException, InterruptedException {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        update( context, path, state, ( response ) -> {
            if ( response.responseType() == ResourceResponse.ResponseType.UPDATED ) {
                try {
                    future.complete( encode( context, response.resource() ) );
                } catch ( Exception e ) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if ( response instanceof ResourceErrorResponse ) {
                handleError( ( ResourceErrorResponse ) response, future );
            } else {
                future.complete( null );
            }
        } );

        return future.get();
    }


    public void delete( RequestContext context, String path, Consumer<ResourceResponse> handler ) {
        ResourceRequest request = new ResourceRequest.Builder( RequestType.DELETE, new ResourcePath( path ) )
                .requestContext( context )
                .build();
        this.handlers.put( request, handler );
        this.channel.writeInbound( request );
    }

    public ResourceState delete( RequestContext context, String path ) throws ResourceException, ExecutionException, InterruptedException {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        delete( context, path, ( response ) -> {
            if ( response.responseType() == ResourceResponse.ResponseType.DELETED ) {
                try {
                    future.complete( encode( context, response.resource() ) );
                } catch ( Exception e ) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else if ( response instanceof ResourceErrorResponse ) {
                handleError( ( ResourceErrorResponse ) response, future );
            } else {
                future.complete( null );
            }
        } );

        try {
            return future.get();
        } catch ( ExecutionException e ) {
            if ( e.getCause() instanceof ResourceException ) {
                throw ( ResourceException ) e.getCause();
            }
            throw e;
        }
    }


    protected ResourceState encode( RequestContext context, Resource resource ) throws Exception {
        CompletableFuture<ResourceState> state = new CompletableFuture<>();

        ResourceStateEncoder encoder = new ResourceStateEncoder();
        RootEncodingDriver driver = new RootEncodingDriver( context, encoder, resource, () -> {
            state.complete( encoder.root() );
        } );

        driver.encode();

        return state.get();
    }

    void handleError( ResourceErrorResponse response, CompletableFuture<?> future ) {
        switch ( ( ( ResourceErrorResponse ) response ).errorType() ) {
            case NOT_AUTHORIZED:
                future.completeExceptionally( new NotAuthorizedException( response.inReplyTo().resourcePath().toString() ) );
                break;
            case NOT_ACCEPTABLE:
                break;
            case NO_SUCH_RESOURCE:
                future.completeExceptionally( new ResourceNotFoundException( response.inReplyTo().resourcePath().toString() ) );
                break;
            case RESOURCE_ALREADY_EXISTS:
                future.completeExceptionally( new ResourceAlreadyExistsException( response.inReplyTo().state().id() ) );
                break;
            case CREATE_NOT_SUPPORTED:
                future.completeExceptionally( new CreateNotSupportedException( response.inReplyTo().resourcePath().toString() ) );
                break;
            case READ_NOT_SUPPORTED:
                future.completeExceptionally( new ReadNotSupportedException( response.inReplyTo().resourcePath().toString() ) );
                break;
            case UPDATE_NOT_SUPPORTED:
                future.completeExceptionally( new UpdateNotSupportedException( response.inReplyTo().resourcePath().toString() ) );
                break;
            case DELETE_NOT_SUPPORTED:
                future.completeExceptionally( new DeleteNotSupportedException( response.inReplyTo().resourcePath().toString() ) );
                break;
        }
    }

    void dispatch( Object obj ) {
        if ( obj instanceof ResourceResponse ) {
            Consumer<ResourceResponse> handler = this.handlers.remove( ( ( ResourceResponse ) obj ).inReplyTo() );
            if ( handler != null ) {
                handler.accept( ( ResourceResponse ) obj );
            }
        }
    }

    private DefaultContainer container;
    private final EmbeddedChannel channel;
    private Map<ResourceRequest, Consumer<ResourceResponse>> handlers = new HashMap<>();

}
