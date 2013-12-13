package io.liveoak.container;

import io.liveoak.spi.Container;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.async.Notifier;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class DefaultResourceContext implements ResourceContext {

    public DefaultResourceContext(Container container, Client client, Vertx vertx, Notifier notifier) {
        this.container = container;
        this.client = client;
        this.vertx = vertx;
        this.notifier = notifier;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public Container container() {
        return this.container;
    }

    @Override
    public Notifier notifier() {
        return this.notifier;
    }

    @Override
    public Client client() {
        return this.client;
    }

    private Container container;
    private Client client;
    private Vertx vertx;
    private Notifier notifier;

}
