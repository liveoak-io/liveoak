package io.liveoak.stomp.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.liveoak.stomp.StompMessage;

import java.util.List;

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
            if (msg.isError()) {
                out.add(StompFrame.newErrorFrame(msg.retain()));
            } else {
                out.add(StompFrame.newMessageFrame(msg.retain()));
            }
        } else {
            out.add(StompFrame.newSendFrame(msg.retain()));
        }
    }

    private boolean server;
}
