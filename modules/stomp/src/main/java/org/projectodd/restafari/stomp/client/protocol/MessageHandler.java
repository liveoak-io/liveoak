package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.*;
import org.projectodd.restafari.stomp.client.ClientContext;
import org.projectodd.restafari.stomp.client.SubscriptionHandler;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompContentFrame;
import org.projectodd.restafari.stomp.common.StompFrame;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class MessageHandler extends AbstractFrameHandler {

    public MessageHandler(ClientContext clientContext, Executor executor) {
        super(Stomp.Command.MESSAGE );
        this.clientContext = clientContext;
        this.executor = executor;
    }

    @Override
    protected void handleFrame(ChannelHandlerContext ctx, StompFrame frame) throws Exception {
        if ( frame instanceof StompContentFrame) {
            StompMessage message = new DefaultStompMessage( frame.getHeaders(), ((StompContentFrame) frame).getContent() );
            String subscriptionId = frame.getHeader( Headers.SUBSCRIPTION );
            SubscriptionHandler handler = this.clientContext.getSubscriptionHandler(subscriptionId);
            this.executor.execute( ()->{
                handler.onMessage( message );
            }  );
            handler.onMessage( message );
        }
    }

    private Executor executor;
    private ClientContext clientContext;
}
