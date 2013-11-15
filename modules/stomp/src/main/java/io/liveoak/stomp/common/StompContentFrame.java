/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;

/**
 * @author Bob McWhirter
 */
public class StompContentFrame extends StompFrame implements ByteBufHolder {

    public StompContentFrame(Stomp.Command command) {
        super( command );
    }

    public StompContentFrame(Stomp.Command command, Headers headers) {
        super( command, headers );
    }

    public StompContentFrame(FrameHeader header) {
        super( header );
    }

    public StompContentFrame(FrameHeader header, ByteBuf content) {
        super( header );
        this.content = content;
    }

    public StompContentFrame(FrameHeader header, String content) {
        super( header);
        this.content = Unpooled.copiedBuffer(content.getBytes());
    }

    public void content(ByteBuf content) {
        this.content = content;
    }

    public ByteBuf content() {
        return this.content;
    }

    @Override
    public StompContentFrame copy() {
        return new StompContentFrame( frameHeader(), this.content.copy() );
    }

    @Override
    public StompContentFrame duplicate() {
        return new StompContentFrame( frameHeader(), this.content.duplicate() );
    }

    @Override
    public int refCnt() {
        return this.content.refCnt();
    }

    @Override
    public StompContentFrame retain() {
        this.content.retain();
        return this;
    }

    @Override
    public StompContentFrame retain(int increment) {
        this.content.retain( increment );
        return this;
    }

    @Override
    public boolean release() {
        return this.content.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.content.release( decrement );
    }

    public String toString() {
        return "[StompContentFrame: header=" + frameHeader() + "; content=" + this.content + " (" + this.content.refCnt() + ", " + this.content.readableBytes() + ")]";
    }

    private ByteBuf content;
}

