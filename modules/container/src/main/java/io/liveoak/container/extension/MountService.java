package io.liveoak.container.extension;

import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class MountService<T extends RootResource> implements Service<T> {

    public MountService() {
    }

    public MountService(String verifyId) {
        this.verifyId = verifyId;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (this.verifyId != null) {
            if (!this.resourceInjector.getValue().id().equals(this.verifyId)) {
                throw new StartException("Invalid ID for resource '" + this.resourceInjector.getValue().id() + "' must be '" + this.verifyId + "'");
            }
        }

        mountResource();
        this.resourceInjector.getValue().parent(this.mountableInjector.getValue());
    }

    protected void mountResource() {
        this.mountableInjector.getValue().registerResource(this.resourceInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resourceInjector.getValue().parent(null);
        unmountResource();
    }

    protected void unmountResource() {
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
    protected InjectedValue<MountPointResource> mountableInjector = new InjectedValue<>();
    protected InjectedValue<T> resourceInjector = new InjectedValue<>();
}
