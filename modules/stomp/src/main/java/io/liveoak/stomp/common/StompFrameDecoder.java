/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import java.nio.charset.Charset;
import java.util.List;

import io.liveoak.stomp.Stomp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * @author Bob McWhirter
 */
public class StompFrameDecoder extends ReplayingDecoder<Void> {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        FrameHeader header = decodeHeader(in);

        int len = header.getContentLength();

        ByteBuf content = null;

        if (len <= 0) {
            content = readUntilNull(in);
        } else {
            content = readUntil(in, len);
        }

        StompFrame frame = null;

        if (content != null) {
            if (header.isContentFrame()) {
                frame = new StompContentFrame(header, content);
            } else {
                frame = new StompControlFrame(header);
            }
        }

        out.add(frame);
    }

    protected ByteBuf readUntilNull(ByteBuf buffer) {
        int nonNullBytes = buffer.bytesBefore((byte) 0x00);

        ByteBuf content = null;
        if (nonNullBytes == 0) {
            content = Unpooled.EMPTY_BUFFER;
        } else {
            content = buffer.readBytes(nonNullBytes);
        }

        buffer.readByte();
        return content;
    }

    protected ByteBuf readUntil(ByteBuf buffer, int len) {
        if (buffer.readableBytes() < (len + 1)) {
            return null;
        }

        ByteBuf content = buffer.readBytes(len);
        buffer.readByte();
        return content;
    }

    protected FrameHeader decodeHeader(ByteBuf buffer) {
        FrameHeader header = null;

        while (header == null || buffer.isReadable()) {
            int nonNewLineBytes = buffer.bytesBefore((byte) '\n');

            if (nonNewLineBytes == 0) {
                buffer.readByte();
                break;
            }
            if (nonNewLineBytes >= 0) {
                ByteBuf line = buffer.readBytes(nonNewLineBytes);
                buffer.readByte();
                header = processHeaderLine(header, line.toString(UTF_8));
            }
        }

        return header;
    }

    protected FrameHeader processHeaderLine(FrameHeader header, String line) {
        if (header == null) {
            header = new FrameHeader();
            Stomp.Command command = Stomp.Command.valueOf(line);
            header.setCommand(command);
            return header;
        }

        int colonLoc = line.indexOf(":");
        if (colonLoc > 0) {
            String name = line.substring(0, colonLoc);
            String value = line.substring(colonLoc + 1);
            header.set(name, value);
        }

        return header;
    }


}
