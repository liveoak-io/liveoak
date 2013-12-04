package io.liveoak.container.deploy;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class JBossModulesDeployer extends AbstractClassLoaderBasedDeployer {

    public JBossModulesDeployer(DefaultContainer container) {
        super( container );
    }

    @Override
    public RootResource deploy(ResourceState state) throws Exception {
        String resourceId = state.id();
        String moduleId = (String) state.getProperty("module-id");
        String className = (String) state.getProperty("class-name");

        Config resourceConfig = new SimpleConfig();

        ModuleLoader loader = ModuleLoader.forClass(JBossModulesDeployer.class);

        Module module = loader.loadModule(ModuleIdentifier.create(moduleId));
        return constructAndRegister(resourceId, className, module.getClassLoader(), resourceConfig );
    }


}
