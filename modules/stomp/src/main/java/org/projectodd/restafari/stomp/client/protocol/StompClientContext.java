package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.Channel;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.StompClient;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public interface StompClientContext {
    void setConnectionState(StompClient.ConnectionState connectionState);
    void setChannel(Channel channel);
    void setVersion(Stomp.Version version);

    String getHost();
    StompClient getClient();
    Consumer<StompMessage> getSubscriptionHandler(String subscriptionId);
}
