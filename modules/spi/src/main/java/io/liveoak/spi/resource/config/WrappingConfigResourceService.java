package io.liveoak.spi.resource.config;

import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class WrappingConfigResourceService implements Service<WrappingConfigResource> {

    public WrappingConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new WrappingConfigResource( this.id, this.configurableInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public WrappingConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<Resource> configurableInjector() {
        return this.configurableInjector;
    }

    private String id;
    private InjectedValue<Resource> configurableInjector = new InjectedValue<>();
    private WrappingConfigResource resource;
}
