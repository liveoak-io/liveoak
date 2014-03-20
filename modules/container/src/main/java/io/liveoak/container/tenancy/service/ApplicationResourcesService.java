package io.liveoak.container.tenancy.service;

import java.util.Set;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ApplicationResourcesService implements Service<Void> {

    public ApplicationResourcesService(ResourceState resourcesTree) {
        this.resourcesTree = resourcesTree;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (this.resourcesTree == null) {
            return;
        }

        try {
            Set<String> fields = this.resourcesTree.getPropertyNames();
            for (String resourceId : fields) {
                ResourceState resourceState = (ResourceState) this.resourcesTree.getProperty(resourceId);
                System.err.println( "BOOTTIME INSTALL OF: " + resourceId );
                this.applicationInjector.getValue().extend(resourceId, resourceState, true);
            }
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private ResourceState resourcesTree;
    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
