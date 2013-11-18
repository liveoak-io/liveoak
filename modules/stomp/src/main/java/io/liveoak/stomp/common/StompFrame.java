/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.netty.buffer.Unpooled;

/**
 * A base STOMP frame.
 *
 * @author Bob McWhirter
 */
public class StompFrame {

    /**
     * Create a new outbound frame.
     *
     * @param command The command for this frame.
     */
    public StompFrame( Stomp.Command command ) {
        this.header = new FrameHeader( command );
    }

    public StompFrame( Stomp.Command command, Headers headers ) {
        this.header = new FrameHeader( command, headers );
    }

    public StompFrame( FrameHeader header ) {
        this.header = header;
    }

    public static StompFrame newAckFrame( Headers headers ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.ACK );
        frame.headers().put( Headers.MESSAGE_ID, headers.get( Headers.MESSAGE_ID ) );
        frame.headers().put( Headers.SUBSCRIPTION, headers.get( Headers.SUBSCRIPTION ) );
        String transactionId = headers.get( Headers.TRANSACTION );
        if ( transactionId != null ) {
            frame.headers().put( Headers.TRANSACTION, transactionId );
        }
        return frame;
    }

    public static StompFrame newNackFrame( Headers headers ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.NACK );
        frame.headers().put( Headers.MESSAGE_ID, headers.get( Headers.MESSAGE_ID ) );
        frame.headers().put( Headers.SUBSCRIPTION, headers.get( Headers.SUBSCRIPTION ) );
        String transactionId = headers.get( Headers.TRANSACTION );
        if ( transactionId != null ) {
            frame.headers().put( Headers.TRANSACTION, transactionId );
        }
        return frame;
    }

    public static StompFrame newSendFrame( StompMessage message ) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.SEND, message.headers() );
        frame.content( message.content() );
        return frame;
    }

    public static StompFrame newMessageFrame( StompMessage message ) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.MESSAGE, message.headers() );
        frame.content( message.content() );
        return frame;
    }

    public static StompFrame newErrorFrame( StompMessage message ) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.ERROR, message.headers() );
        frame.content( message.content() );
        return frame;
    }

    public static StompFrame newConnectedFrame( String sessionId, Stomp.Version version ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.CONNECTED );
        frame.headers().put( Headers.SESSION, sessionId );
        String implVersion = "0.1";
        frame.headers().put( Headers.SERVER, "mboss/" + implVersion );
        if ( version.isAfter( Stomp.Version.VERSION_1_0 ) ) {
            frame.headers().put( Headers.VERSION, version.versionString() );
        }
        return frame;
    }

    public static StompFrame newDisconnectFrame() {
        StompFrame frame = new StompControlFrame( Stomp.Command.DISCONNECT );
        frame.headers().put( Headers.RECEIPT, "connection-close" );
        return frame;
    }

    public static StompFrame newErrorFrame( String message, StompFrame inReplyTo ) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.ERROR );
        if ( inReplyTo != null ) {
            String receiptId = inReplyTo.headers().get( Headers.RECEIPT );
            if ( receiptId != null ) {
                frame.headers().put( Headers.RECEIPT_ID, receiptId );
            }
        }
        byte[] bytes = message.getBytes();
        frame.content( Unpooled.copiedBuffer( bytes ) );
        frame.headers().put( Headers.CONTENT_LENGTH, String.valueOf( bytes.length ) );
        frame.headers().put( Headers.CONTENT_TYPE, "text/plain" );
        return frame;
    }

    public static StompFrame newReceiptFrame( String receiptId ) {
        StompControlFrame receipt = new StompControlFrame( Stomp.Command.RECEIPT );
        receipt.headers().put( Headers.RECEIPT_ID, receiptId );
        return receipt;
    }

    public static StompControlFrame newBeginFrame( String transactionId ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.BEGIN );
        frame.headers().put( Headers.TRANSACTION, transactionId );
        return frame;
    }

    public static StompControlFrame newCommitFrame( String transactionId ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.COMMIT );
        frame.headers().put( Headers.TRANSACTION, transactionId );
        return frame;
    }

    public static StompControlFrame newAbortFrame( String transactionId ) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.ABORT );
        frame.headers().put( Headers.TRANSACTION, transactionId );
        return frame;
    }

    public Stomp.Command command() {
        return this.header.command();
    }

    public Headers headers() {
        return this.header.getMap();
    }

    public FrameHeader frameHeader() {
        return this.header;
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": header=" + this.header + "]";
    }

    private FrameHeader header;

}
