package io.liveoak.container.deploy;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class ClasspathDeployer extends AbstractClassLoaderBasedDeployer {

    public ClasspathDeployer(DefaultContainer container) {
        super(container);
    }

    @Override
    public RootResource deploy(ResourceState state) throws Exception {
        String resourceId = state.id();
        String className = (String) state.getProperty("class-name");

        ResourceState resourceConfig = (ResourceState) state.getProperty( "config" );

        ModuleLoader loader = ModuleLoader.forClass(ClasspathDeployer.class);

        ClassLoader classLoader = null;
        Module module = Module.forClass(ClasspathDeployer.class);

        if ( module == null ) {
            classLoader = getClass().getClassLoader();
        } else {
            classLoader = module.getClassLoader();
        }

        return constructAndRegister(resourceId, className, classLoader, resourceConfig);
    }

    private DefaultContainer container;

}
