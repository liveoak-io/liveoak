package org.projectodd.restafari.container.eventbus;

import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.InMemoryObjectResourceController;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class EventBusBridgeContainerTest {

    @Test
    public void testEventBus() throws Exception {
        InMemoryObjectResourceController controller = new InMemoryObjectResourceController();
        controller.addCollection("people");
        controller.addCollection("dogs");

        Container container = new EventBusBridgeContainer();
        container.registerResourceController("memory", controller, new SimpleConfig());

        UnsecureServer server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
        server.start();

        Vertx vertx = container.getVertx();
        EventBus eventBus = vertx.eventBus();

        CountDownLatch responseLatch = new CountDownLatch(1);
        System.err.println("!!! Sending EventBus Message");
        eventBus.send("memory.get", "people", (Message<JsonObject> message) -> {
                System.err.println("!!! Handling EventBus response: " + message.body());
                responseLatch.countDown();
        });
        System.err.println("!!! Sent EventBus Message");
        assertTrue(responseLatch.await(5, TimeUnit.SECONDS));
        server.stop();
    }
}
