package org.projectodd.restafari.stomp.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;

import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class StompMessageEncoder extends MessageToMessageEncoder<StompMessage> {

    public StompMessageEncoder(boolean server) {
        this.server = server;

    }

    @Override
    protected void encode(ChannelHandlerContext ctx, StompMessage msg, List<Object> out) throws Exception {
        if (server) {
            out.add(StompFrame.newMessageFrame(msg) );
        } else {
            out.add(StompFrame.newSendFrame(msg));
        }
    }

    private boolean server;
}
