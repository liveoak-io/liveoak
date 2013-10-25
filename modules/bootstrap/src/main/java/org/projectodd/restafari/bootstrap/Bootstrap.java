package org.projectodd.restafari.bootstrap;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.projectodd.restafari.bootstrap.deployer.ConfigDeployer;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.projectodd.restafari.container.resource.ContainerResource;
import org.projectodd.restafari.spi.resource.RootResource;

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
