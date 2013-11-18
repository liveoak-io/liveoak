/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

import org.vertx.java.core.Vertx;

/**
 * Resource initialization context.
 *
 * @author Bob McWhirter
 */
public interface ResourceContext {

    /**
     * Retrieve the Vertx.
     *
     * @return The Vertx.
     */
    Vertx vertx();

    /**
     * Retrieve the container.
     *
     * @return The container.
     */
    Container container();

    /**
     * Retrieve the controller config.
     *
     * @return The controller config.
     */
    Config config();

}
