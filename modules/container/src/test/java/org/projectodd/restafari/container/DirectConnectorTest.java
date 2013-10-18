package org.projectodd.restafari.container;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Bob McWhirter
 */
public class DirectConnectorTest {

    private DefaultContainer container;
    private DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = this.container.directConnector();
    }

    @Test
    public void testConnectorRead() {

        this.connector.read("/", (response) -> {
            System.err.println( "response to slash: " + response );
        });

        this.connector.read("/tacos", (response) -> {
            System.err.println( "response to tacos: " + response );
        });

    }
}
