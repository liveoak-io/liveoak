package io.liveoak.testtools;

import org.junit.Before;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.DirectConnector;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import org.vertx.java.core.Vertx;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractResourceTestCase {

    private DefaultContainer container;
    protected DirectConnector connector;
    protected Vertx vertx;


    public abstract RootResource createRootResource();

    public Config createConfig() {
        return new SimpleConfig();
    }

    @Before
    public void setUpContainer() throws Exception {
        this.container = new DefaultContainer();
        this.container.registerResource( createRootResource(), createConfig() );
        this.connector = this.container.directConnector();
        this.vertx = this.container.vertx();
    }
}
