/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.DirectConnector;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import org.junit.After;
import org.junit.Before;
import org.vertx.java.core.Vertx;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractResourceTestCase {

    private DefaultContainer container;
    protected DirectConnector connector;
    protected RootResource resource;
    protected Vertx vertx;


    public abstract RootResource createRootResource();

    public Config createConfig() {
        return new SimpleConfig();
    }

    @Before
    public void setUpContainer() throws Exception {
        this.container = new DefaultContainer();
        this.resource = createRootResource();
        this.container.registerResource( this.resource, createConfig() );
        this.connector = this.container.directConnector();
        this.vertx = this.container.vertx();
    }

    @After
    public void shutdownContainer() throws Exception {
        this.vertx.stop();
        this.container.shutdown();
    }

}
