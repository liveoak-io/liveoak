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
public class ClasspathBasedResourceFactory implements RootResourceFactory {

    @Override
    public String type() {
        return "classpath";
    }

    @Override
    public RootResource createResource(String id, ResourceState descriptor) throws Exception {

        String className = (String) descriptor.getProperty( "class-name" );
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) classLoader.loadClass( className );

        Constructor<? extends RootResource> ctor = resourceClass.getConstructor(String.class);

        return ctor.newInstance( id );
    }
}
