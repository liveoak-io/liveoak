package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.Channel;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.StompClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public interface ClientContext {
    void setConnectionState(StompClient.ConnectionState connectionState);
    void setChannel(Channel channel);
    void setVersion(Stomp.Version version);

    String getHost();
    StompClient getClient();
    Consumer<StompMessage> getSubscriptionHandler(String subscriptionId);
}
