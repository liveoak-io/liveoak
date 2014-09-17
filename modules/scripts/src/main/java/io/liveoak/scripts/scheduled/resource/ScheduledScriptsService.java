package io.liveoak.scripts.scheduled.resource;

import io.liveoak.scripts.scheduled.manager.ScheduleManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptsService implements Service<ScheduledScriptsResource> {

    ScheduledScriptsResource scheduledScripts;

    @Override
    public void start(StartContext startContext) throws StartException {
        this.scheduledScripts = new ScheduledScriptsResource(vertxInjector.getValue(), scheduleManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        scheduledScripts = null;
    }

    @Override
    public ScheduledScriptsResource getValue() throws IllegalStateException, IllegalArgumentException {
        return scheduledScripts;
    }

    public InjectedValue<Vertx> vertxInjector = new InjectedValue<>();

    public InjectedValue<ScheduleManager> scheduleManagerInjector = new InjectedValue<>();
}
