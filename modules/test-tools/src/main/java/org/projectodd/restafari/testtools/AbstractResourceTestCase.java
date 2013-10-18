package org.projectodd.restafari.testtools;

import org.junit.Before;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.DirectConnector;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractResourceTestCase {

    private DefaultContainer container;
    protected DirectConnector connector;

    public abstract RootResource createRootResource();

    public Config createConfig() {
        return new SimpleConfig();
    }

    @Before
    public void setUpContainer() throws Exception {
        this.container = new DefaultContainer();
        this.container.registerResource( createRootResource(), createConfig() );
        this.connector = this.container.directConnector();
    }
}
