package org.projectodd.restafari.container.protocols.stomp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.stomp.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.StompControlFrame;
import org.projectodd.restafari.stomp.StompFrame;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class ConnectHandler extends AbstractControlFrameHandler {

    static final AttributeKey<String> CONNECTION_ID = new AttributeKey<>("connection_id");

    public ConnectHandler() {
        super(StompFrame.Command.CONNECT);
    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws Exception {
        String connectionId = UUID.randomUUID().toString();
        ctx.attr(CONNECTION_ID).set(connectionId);
    }
}
