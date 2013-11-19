/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.subscriptions.StompSubscription;
import io.liveoak.spi.MediaType;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.server.StompConnection;
import io.liveoak.stomp.server.StompServerContext;

/**
 * @author Bob McWhirter
 */
public class ContainerStompServerContext implements StompServerContext {

    public ContainerStompServerContext( DefaultContainer container ) {
        this.container = container;
    }

    @Override
    public void handleConnect( StompConnection connection ) {
    }

    @Override
    public void handleDisconnect( StompConnection connection ) {
    }

    @Override
    public void handleSubscribe( StompConnection connection, String destination, String subscriptionId, Headers headers ) {
        String acceptMediaType = headers.get( "accept" );
        if ( acceptMediaType == null ) {
            acceptMediaType = "application/json";
        }
        MediaType mediaType = this.container.getCodecManager().determineMediaType( acceptMediaType, null );
        if ( mediaType == null ) {
            mediaType = MediaType.JSON;
        }

        ResourceCodec codec = this.container.getCodecManager().getResourceCodec( mediaType );
        StompSubscription subscription = new StompSubscription( this.container.getSubscriptionManager(), connection, destination, subscriptionId, mediaType, codec );
        this.container.getSubscriptionManager().addSubscription( subscription );
    }

    @Override
    public void handleUnsubscribe( StompConnection connection, String subscriptionId ) {
    }

    @Override
    public void handleSend( StompConnection connection, StompMessage message ) {
    }

    private DefaultContainer container;
}
