package io.liveoak.container.deploy;

import java.lang.reflect.InvocationTargetException;

import io.liveoak.container.DefaultContainer;
import io.liveoak.spi.Config;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.modules.Module;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractClassLoaderBasedDeployer extends AbstractClassBasedDeployer {

    public AbstractClassLoaderBasedDeployer(DefaultContainer container) {
        super( container );
    }

    protected RootResource constructAndRegister(String resourceId, String className, ClassLoader classLoader, ResourceState config) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, InitializationException {
        Class<? extends RootResource> resourceClass = (Class<? extends RootResource>) classLoader.loadClass(className);
        RootResource resource = construct(resourceId, resourceClass);
        register( resource, config );
        return resource;
    }
}
