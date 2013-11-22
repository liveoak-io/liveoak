/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompException;
import io.liveoak.stomp.StompMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * @author Bob McWhirter
 */
public class DefaultStompMessage implements StompMessage, ByteBufHolder {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public DefaultStompMessage() {
        this(new HeadersImpl());
    }

    public DefaultStompMessage(Headers headers) {
        this(headers, Unpooled.EMPTY_BUFFER, false);
    }

    public DefaultStompMessage(Headers headers, ByteBuf content) {
        this(headers, content, false);

    }

    public DefaultStompMessage(boolean error) {
        this(new HeadersImpl(), Unpooled.EMPTY_BUFFER, error);
    }

    public DefaultStompMessage(Headers headers, ByteBuf content, boolean error) {
        this.content = content;
        this.headers = headers;
        this.error = error;
    }

    @Override
    public String id() {
        return this.headers.get(Headers.MESSAGE_ID);
    }

    @Override
    public Headers headers() {
        return this.headers;
    }

    @Override
    public String destination() {
        return this.headers.get(Headers.DESTINATION);
    }

    @Override
    public void destination(String destination) {
        this.headers.put(Headers.DESTINATION, destination);
    }

    @Override
    public String contentType() {
        return this.headers.get(Headers.CONTENT_TYPE);
    }

    @Override
    public void contentType(String contentType) {
        this.headers.put(Headers.CONTENT_TYPE, contentType);
    }

    @Override
    public String content(Charset charset) {
        return this.content.duplicate().retain().toString(charset);
    }

    @Override
    public String utf8Content() {
        return content(UTF_8);
    }

    @Override
    public void content(String content, Charset charset) {
        if (this.content != null) {
            this.content().release();
        }
        this.content = Unpooled.copiedBuffer(content.toCharArray(), charset).retain();
    }

    public void content(String content) {
        content(content, UTF_8);
    }

    @Override
    public void content(ByteBuf content) {
        if (this.content != null) {
            this.content.release();
        }
        this.content = content;
    }

    @Override
    public ByteBuf content() {
        return this.content;
    }

    @Override
    public boolean isError() {
        return this.error;
    }

    @Override
    public void ack() throws StompException {
    }

    @Override
    public void nack() throws StompException {
    }

    @Override
    public void ack(String transactionId) throws StompException {
    }

    @Override
    public void nack(String transactionId) throws StompException {
    }

    public StompMessage duplicate() {
        return new DefaultStompMessage(this.headers.duplicate(), this.content.duplicate(), this.error);
    }

    @Override
    public ByteBufHolder copy() {
        return new DefaultStompMessage(this.headers.duplicate(), this.content.copy(), this.error);
    }

    @Override
    public int refCnt() {
        return this.content.refCnt();
    }

    @Override
    public StompMessage retain() {
        this.content.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int increment) {
        this.content.retain(increment);
        return this;
    }

    @Override
    public boolean release() {
        return this.content.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.content.release(decrement);
    }

    public String toString() {
        return "[DefaultStompMessage: headers=" + this.headers + "; error=" + error + "; content=" + this.content + "," + this.content.refCnt() + "]";
    }


    private Headers headers;
    private ByteBuf content;
    private boolean error;
}
