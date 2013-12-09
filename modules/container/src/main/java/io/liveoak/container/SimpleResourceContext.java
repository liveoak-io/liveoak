/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.Config;
import io.liveoak.spi.Container;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.async.Notifier;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SimpleResourceContext implements ResourceContext {

    private final Vertx vertx;
    private final String id;
    private final Container container;
    private final Notifier notifier;

    public SimpleResourceContext(String id, Vertx vertx, DefaultContainer container) {
        this.id = id;
        this.vertx = vertx;
        this.container = container;
        this.notifier = new NotifierImpl(container.getSubscriptionManager());
    }

    @Override
    public String id() {
        return this.id;
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
    public Notifier notifier() {
        return notifier;
    }


}
