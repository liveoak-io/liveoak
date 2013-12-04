package io.liveoak.container.deploy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.liveoak.container.DefaultContainer;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractClassBasedDeployer extends AbstractDeployer {

    public AbstractClassBasedDeployer(DefaultContainer container) {
        super( container );
    }

    protected RootResource construct(String resourceId, Class<? extends RootResource> resourceClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends RootResource> ctor = null;
        try {
            ctor = resourceClass.getConstructor(String.class);
            return ctor.newInstance(resourceId);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            ctor = resourceClass.getConstructor();
            return ctor.newInstance();
        }
    }
}
