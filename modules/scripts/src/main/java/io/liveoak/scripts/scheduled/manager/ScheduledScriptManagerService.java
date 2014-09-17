package io.liveoak.scripts.scheduled.manager;

import io.liveoak.scripts.libraries.manager.LibraryManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptManagerService implements Service<ScheduledScriptManager>{

    ScheduledScriptManager manager;

    @Override
    public void start(StartContext startContext) throws StartException {
        this.manager = new ScheduledScriptManager(libraryManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        manager = null;
    }

    @Override
    public ScheduledScriptManager getValue() throws IllegalStateException, IllegalArgumentException {
        return manager;
    }

    public InjectedValue<LibraryManager> libraryManagerInjector = new InjectedValue<>();
}
