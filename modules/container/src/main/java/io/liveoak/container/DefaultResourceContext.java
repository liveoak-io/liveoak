package io.liveoak.container;

import io.liveoak.spi.Container;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.async.Notifier;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class DefaultResourceContext implements ResourceContext {

    public DefaultResourceContext(Container container, Vertx vertx, Notifier notifier) {
        this.container = container;
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

    private Container container;
    private Vertx vertx;
    private Notifier notifier;

}
