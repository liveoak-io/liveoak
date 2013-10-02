package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.Container;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SimpleControllerContext implements ControllerContext {

    private final Vertx vertx;
    private final Container container;
    private final Config config;

    public SimpleControllerContext(Vertx vertx, Container container, Config config) {
        this.vertx = vertx;
        this.container = container;
        this.config = config;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public Config getConfig() {
        return config;
    }
}
