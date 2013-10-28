package org.projectodd.restafari.testtools;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.resource.RootResource;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractHTTPResourceTestCase {

    private UnsecureServer server;
    private DefaultContainer container;
    protected CloseableHttpClient httpClient;

    public abstract RootResource createRootResource();

    public Config createConfig() {
        return new SimpleConfig();
    }

    @Before
    public void setUpClient() throws Exception {
        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(cconfig).build();
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
