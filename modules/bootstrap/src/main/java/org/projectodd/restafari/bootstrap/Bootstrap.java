package org.projectodd.restafari.bootstrap;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
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

        ModuleLoader loader = ModuleLoader.forClass(Bootstrap.class);
        Module module = loader.loadModule(ModuleIdentifier.create("org.projectodd.restafari.filesystem"));

        Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) module.getClassLoader().loadClass( "org.projectodd.restafari.filesystem.FilesystemResource");

        RootResource resource = resourceClass.newInstance();
        SimpleConfig fsConfig = new SimpleConfig();
        fsConfig.put( "id", "assets" );
        fsConfig.put( "root", "." );
        container.registerResource( resource, fsConfig );

        server.start();
    }
}
