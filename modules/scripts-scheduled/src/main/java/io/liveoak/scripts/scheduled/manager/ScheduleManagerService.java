package io.liveoak.scripts.scheduled.manager;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.quartz.Scheduler;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduleManagerService implements Service<ScheduleManager> {

    private ScheduleManager manager;

    @Override
    public void start(StartContext startContext) throws StartException {
        this.manager = new ScheduleManager(schedulerInjector.getValue(), scheduledScriptManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        manager = null;
    }

    @Override
    public ScheduleManager getValue() throws IllegalStateException, IllegalArgumentException {
        return manager;
    }

    public InjectedValue<Scheduler> schedulerInjector = new InjectedValue<>();

    public InjectedValue<ScheduledScriptManager> scheduledScriptManagerInjector = new InjectedValue<>();
}
