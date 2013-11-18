/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.container.UnsecureServer;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractHTTPResourceTestCase extends AbstractTestCase {

    private UnsecureServer server;
    private DefaultContainer container;
    protected CloseableHttpClient httpClient;

    public abstract RootResource createRootResource();

    public Config createConfig() {
        return new SimpleConfig();
    }

    @Before
    public void setUpClient() throws Exception {
        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout( 500000 ).build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig( cconfig ).build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @Before
    public void setUpServer() throws Exception {
        this.container = new DefaultContainer();
        this.container.registerResource( createRootResource(), createConfig() );
        this.server = new UnsecureServer( this.container, "localhost", 8080 );
        this.server.start();
    }

    @After
    public void tearDownServer() throws Exception {
        this.server.stop();
    }
}
