package io.liveoak.container.tenancy.service;

import java.util.Set;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
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
public class ApplicationResourcesStartupService implements Service<Void> {

    public ApplicationResourcesStartupService(ResourceState resourcesTree) {
        this.resourcesTree = resourcesTree;
    }

    @Override
    public void start(StartContext context) throws StartException {
        log.debug("application resources service: START: " + this.resourcesTree);
        if (this.resourcesTree == null) {
            return;
        }

        context.asynchronous();
        try {
            new Thread(() -> {
                try {
                    Set<String> fields = this.resourcesTree.getPropertyNames();
                    for (String resourceId : fields) {
                        ResourceState resourceState = (ResourceState) this.resourcesTree.getProperty(resourceId);
                        log.debug("BOOTTIME INSTALL OF: " + resourceId);
                        this.applicationInjector.getValue().extend(resourceId, resourceState, true);
                    }
                    context.complete();

                } catch (Throwable e) {
                    context.failed(new StartException(e));
                }
            }, "ApplicationResourcesService starter - " + this.applicationInjector.getValue().name()).start();
        } catch (Throwable e) {
            context.failed(new StartException(e));
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

    private static final Logger log = Logger.getLogger(ApplicationResourcesStartupService.class);
}
