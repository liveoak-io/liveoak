/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liveoak.stomp.common;

import io.netty.buffer.Unpooled;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;

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
    public StompFrame(Stomp.Command command) {
        this.header = new FrameHeader( command );
    }

    public StompFrame(Stomp.Command command, Headers headers) {
        this.header = new FrameHeader( command, headers );
    }

    public StompFrame(FrameHeader header) {
        this.header = header;
    }

    public static StompFrame newAckFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.ACK );
        frame.headers().put(Headers.MESSAGE_ID, headers.get(Headers.MESSAGE_ID));
        frame.headers().put(Headers.SUBSCRIPTION, headers.get(Headers.SUBSCRIPTION));
        String transactionId = headers.get( Headers.TRANSACTION );
        if (transactionId != null) {
            frame.headers().put(Headers.TRANSACTION, transactionId);
        }
        return frame;
    }

    public static StompFrame newNackFrame(Headers headers) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.NACK );
        frame.headers().put(Headers.MESSAGE_ID, headers.get(Headers.MESSAGE_ID));
        frame.headers().put(Headers.SUBSCRIPTION, headers.get(Headers.SUBSCRIPTION));
        String transactionId = headers.get( Headers.TRANSACTION );
        if (transactionId != null) {
            frame.headers().put(Headers.TRANSACTION, transactionId);
        }
        return frame;
    }

    public static StompFrame newSendFrame(StompMessage message) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.SEND, message.headers() );
        frame.content(message.content());
        return frame;
    }

    public static StompFrame newMessageFrame(StompMessage message) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.MESSAGE, message.headers() );
        frame.content(message.content());
        return frame;
    }

    public static StompFrame newErrorFrame(StompMessage message) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.ERROR, message.headers() );
        frame.content( message.content());
        return frame;
    }

    public static StompFrame newConnectedFrame(String sessionId, Stomp.Version version) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.CONNECTED );
        frame.headers().put(Headers.SESSION, sessionId);
        String implVersion = "0.1";
        frame.headers().put(Headers.SERVER, "mboss/" + implVersion);
        if (version.isAfter( Stomp.Version.VERSION_1_0 )) {
            frame.headers().put(Headers.VERSION, version.versionString());
        }
        return frame;
    }

    public static StompFrame newDisconnectFrame() {
        StompFrame frame = new StompControlFrame( Stomp.Command.DISCONNECT );
        frame.headers().put(Headers.RECEIPT, "connection-close");
        return frame;
    }

    public static StompFrame newErrorFrame(String message, StompFrame inReplyTo) {
        StompContentFrame frame = new StompContentFrame( Stomp.Command.ERROR );
        if (inReplyTo != null) {
            String receiptId = inReplyTo.headers().get(Headers.RECEIPT);
            if (receiptId != null) {
                frame.headers().put(Headers.RECEIPT_ID, receiptId);
            }
        }
        byte[] bytes = message.getBytes();
        frame.content(Unpooled.copiedBuffer(bytes));
        frame.headers().put(Headers.CONTENT_LENGTH, String.valueOf(bytes.length));
        frame.headers().put( Headers.CONTENT_TYPE, "text/plain" );
        return frame;
    }

    public static StompFrame newReceiptFrame(String receiptId) {
        StompControlFrame receipt = new StompControlFrame( Stomp.Command.RECEIPT );
        receipt.headers().put(Headers.RECEIPT_ID, receiptId);
        return receipt;
    }

    public static StompControlFrame newBeginFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.BEGIN );
        frame.headers().put(Headers.TRANSACTION, transactionId);
        return frame;
    }

    public static StompControlFrame newCommitFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.COMMIT );
        frame.headers().put(Headers.TRANSACTION, transactionId);
        return frame;
    }

    public static StompControlFrame newAbortFrame(String transactionId) {
        StompControlFrame frame = new StompControlFrame( Stomp.Command.ABORT );
        frame.headers().put(Headers.TRANSACTION, transactionId);
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
