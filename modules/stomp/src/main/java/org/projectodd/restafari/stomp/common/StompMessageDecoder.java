package org.projectodd.restafari.stomp.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class StompMessageDecoder extends MessageToMessageDecoder<StompFrame> {

    @Override
    protected void decode(ChannelHandlerContext ctx, StompFrame msg, List<Object> out) throws Exception {
        if (msg instanceof StompContentFrame) {
            if (msg.command() == Stomp.Command.MESSAGE || msg.command() == Stomp.Command.SEND) {
                StompMessage stompMessage = new DefaultStompMessage(msg.headers(), ((StompContentFrame) msg).content().retain());
                out.add(stompMessage);
            } else {
                ReferenceCountUtil.retain(msg);
                out.add(msg);
            }
        } else {
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        }

    }
}
