/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.Config;
import io.liveoak.spi.Container;
import io.liveoak.spi.ResourceContext;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SimpleResourceContext implements ResourceContext {

    private final Vertx vertx;
    private final Container container;
    private final Config config;

    public SimpleResourceContext( Vertx vertx, Container container, Config config ) {
        this.vertx = vertx;
        this.container = container;
        this.config = config;
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public Container container() {
        return container;
    }

    @Override
    public Config config() {
        return config;
    }
}
