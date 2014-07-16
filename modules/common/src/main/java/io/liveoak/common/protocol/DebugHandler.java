/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.protocol;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class DebugHandler extends ChannelDuplexHandler {

    public DebugHandler(String name) {
        this.name = name;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debugf("%s channel-registered", this.name);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debugf("%s channel-active", this.name);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debugf("%s  channel-inactive", this.name);
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debugf("%s  channel-unregistered", this.name);
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debugf(cause, "%s  exception-caught", this.name);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.debugf("%s write: %s", this.name, msg);
        if (msg instanceof ByteBuf) {
            log.debug(((ByteBuf) msg).toString(Charset.defaultCharset()));
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debugf("%s read : %s // %s", this.name, msg, msg.getClass());
        if (msg instanceof ByteBuf) {
            log.debug(((ByteBuf) msg).toString(Charset.defaultCharset()));
        }
        ReferenceCountUtil.retain(msg);
        super.channelRead(ctx, msg);
    }


    private String name;

    private static final Logger log = Logger.getLogger(DebugHandler.class);
}
