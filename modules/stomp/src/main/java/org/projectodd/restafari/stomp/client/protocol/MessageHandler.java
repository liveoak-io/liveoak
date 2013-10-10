package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.*;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompContentFrame;
import org.projectodd.restafari.stomp.common.StompFrame;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

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
            Consumer<StompMessage> handler = this.clientContext.getSubscriptionHandler(subscriptionId);
            this.executor.execute( ()->{
                handler.accept( message );
            }  );
        }
    }

    private Executor executor;
    private ClientContext clientContext;
}
