package io.liveoak.container.extension;

import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class MountService<T extends RootResource> implements Service<T> {

    public MountService() {

    }

    public MountService(String verifyId) {
        this.verifyId = verifyId;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if ( this.verifyId != null ) {
            if ( !this.resourceInjector.getValue().id().equals( this.verifyId ) ) {
                throw new StartException( "Invalid ID for resource '" + this.resourceInjector.getValue().id() + "' must be '" + this.verifyId + "'" );
            }
        }


        this.mountableInjector.getValue().registerResource( this.resourceInjector.getValue() );
        this.resourceInjector.getValue().parent(this.mountableInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resourceInjector.getValue().parent( null );
        this.mountableInjector.getValue().unregisterResource(this.resourceInjector.getValue());
    }

    @Override
    public T getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resourceInjector.getValue();
    }

    public Injector<MountPointResource> mountPointInjector() {
        return this.mountableInjector;
    }

    public Injector<T> resourceInjector() {
        return this.resourceInjector;
    }

    private String verifyId;
    private InjectedValue<MountPointResource> mountableInjector = new InjectedValue<>();
    private InjectedValue<T> resourceInjector = new InjectedValue<>();
}
