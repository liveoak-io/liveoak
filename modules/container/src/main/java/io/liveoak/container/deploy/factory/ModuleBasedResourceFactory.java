package io.liveoak.container.deploy.factory;

import java.lang.reflect.Constructor;

import io.liveoak.spi.container.RootResourceFactory;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * @author Bob McWhirter
 */
public class ModuleBasedResourceFactory implements RootResourceFactory {

    @Override
    public String type() {
        return "jboss-module";
    }

    @Override
    public RootResource createResource(String id, ResourceState descriptor) throws Exception {

        String moduleId = (String) descriptor.getProperty( "module-id" );
        String className = (String) descriptor.getProperty( "class-name" );

        ModuleLoader loader = ModuleLoader.forClass( getClass() );

        Module module = loader.loadModule(ModuleIdentifier.create( moduleId ) );
        Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) module.getClassLoader().loadClass( className );

        Constructor<? extends RootResource> ctor = resourceClass.getConstructor(String.class);

        return ctor.newInstance( id );
    }
}
