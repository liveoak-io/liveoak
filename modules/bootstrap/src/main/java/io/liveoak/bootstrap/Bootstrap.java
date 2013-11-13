package io.liveoak.bootstrap;

import io.liveoak.bootstrap.deployer.ConfigDeployer;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.container.UnsecureServer;
import io.liveoak.container.resource.ContainerResource;

/**
 * @author Bob McWhirter
 */
public class Bootstrap {

    public static void main(String... args) throws Exception {

        System.err.println("Booting up the mBaaS");

        DefaultContainer container = new DefaultContainer();
        UnsecureServer server = new UnsecureServer(container, "localhost", 8080);

        container.registerResource(new ContainerResource("_container"), new SimpleConfig());

        if ( args.length > 0 ) {
            ConfigDeployer deployer = new ConfigDeployer( container );
            deployer.deploy( args[0] );
        }

        server.start();
    }
}
