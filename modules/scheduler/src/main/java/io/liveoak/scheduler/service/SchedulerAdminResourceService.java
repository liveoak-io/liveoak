package io.liveoak.scheduler.service;

import io.liveoak.scheduler.SchedulerAdminResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.quartz.Scheduler;

/**
 * @author Bob McWhirter
 */
public class SchedulerAdminResourceService implements Service<SchedulerAdminResource> {

    public SchedulerAdminResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new SchedulerAdminResource(
                this.id,
                this.schedulerInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public SchedulerAdminResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<Scheduler> schedulerInjector() {
        return this.schedulerInjector;
    }

    private String id;

    private InjectedValue<Scheduler> schedulerInjector = new InjectedValue<>();

    private SchedulerAdminResource resource;
}
