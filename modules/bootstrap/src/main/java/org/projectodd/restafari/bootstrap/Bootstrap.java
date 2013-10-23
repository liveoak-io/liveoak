package org.projectodd.restafari.bootstrap;

import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.projectodd.restafari.container.resource.ContainerResource;

/**
 * @author Bob McWhirter
 */
public class Bootstrap {

    public static void main(String...args) throws Exception {

        System.err.println( "Booting up the mBaaS" );


        DefaultContainer container = new DefaultContainer();
        UnsecureServer server = new UnsecureServer( container, "localhost", 8080 );

        container.registerResource( new ContainerResource("_container"), new SimpleConfig());

        server.start();
    }
}
