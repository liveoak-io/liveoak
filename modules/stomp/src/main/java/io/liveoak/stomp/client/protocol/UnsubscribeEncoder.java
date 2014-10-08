package io.liveoak.stomp.client.protocol;

import java.util.List;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.client.Unsubscribe;
import io.liveoak.stomp.common.StompControlFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author Ken Finnigan
 */
public class UnsubscribeEncoder extends MessageToMessageEncoder<Unsubscribe> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Unsubscribe msg, List<Object> out) throws Exception {
        StompControlFrame frame = new StompControlFrame(Stomp.Command.UNSUBSCRIBE);
        frame.headers().putAll(msg.headers());
        frame.headers().put(Headers.ID, msg.subscriptionId());
        out.add(frame);
    }
}
