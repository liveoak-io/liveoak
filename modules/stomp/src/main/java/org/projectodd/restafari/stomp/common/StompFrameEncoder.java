package org.projectodd.restafari.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.projectodd.restafari.stomp.Headers;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class StompFrameEncoder extends MessageToByteEncoder<StompFrame> {

    private static final int HEADER_ESTIMATE = 1024;
    private static final byte[] HEADER_DELIM = ":".getBytes();
    private static final byte NEWLINE = (byte) '\n';
    private static final byte NULL = (byte) 0x00;

    @Override
    protected void encode(ChannelHandlerContext ctx, StompFrame frame, ByteBuf out) throws Exception {
        writeHeader(frame, out);
        writeContent(frame, out);
    }

    protected void writeHeader(StompFrame frame, ByteBuf buffer) {
        buffer.writeBytes( frame.getCommand().getBytes() );
        buffer.writeByte( NEWLINE );
        Set<String> headerNames = frame.headers().getHeaderNames();
        for (String name : headerNames) {
            if (name.equalsIgnoreCase( "content-length" )) {
                continue;
            }
            buffer.writeBytes( name.getBytes() );
            buffer.writeBytes( HEADER_DELIM );
            buffer.writeBytes( frame.headers().get( name ).getBytes() );
            buffer.writeByte( NEWLINE );
        }

        if (frame instanceof StompContentFrame) {
            int length = ((StompContentFrame) frame).content().readableBytes();
            buffer.writeBytes(Headers.CONTENT_LENGTH.getBytes());
            buffer.writeBytes( HEADER_DELIM );
            buffer.writeBytes( ("" + length).getBytes() );
            buffer.writeByte( NEWLINE );
        }

        buffer.writeByte( NEWLINE );
    }

    protected void writeContent(StompFrame frame, ByteBuf buffer) {
        if (frame instanceof StompContentFrame) {
            ByteBuf content = ((StompContentFrame) frame).content();
            buffer.writeBytes( content );
        }
        buffer.writeByte( NULL );
        return;
    }
}
