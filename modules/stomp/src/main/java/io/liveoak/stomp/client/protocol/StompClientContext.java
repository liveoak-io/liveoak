package io.liveoak.stomp.client.protocol;

import io.netty.channel.Channel;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;

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
