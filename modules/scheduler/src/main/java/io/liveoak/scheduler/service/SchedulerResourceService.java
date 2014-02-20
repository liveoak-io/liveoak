package io.liveoak.scheduler.service;

import io.liveoak.scheduler.SchedulerResource;
import io.liveoak.spi.resource.async.Notifier;
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
public class SchedulerResourceService implements Service<SchedulerResource> {

    public SchedulerResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new SchedulerResource(
                this.id,
                this.schedulerInjector.getValue(),
                this.notifierInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public SchedulerResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<Scheduler> schedulerInjector() {
        return this.schedulerInjector;
    }

    public Injector<Notifier> notifierInjector() {
        return this.notifierInjector;
    }

    private String id;

    private InjectedValue<Scheduler> schedulerInjector = new InjectedValue<>();
    private InjectedValue<Notifier> notifierInjector = new InjectedValue<>();

    private SchedulerResource resource;
}
