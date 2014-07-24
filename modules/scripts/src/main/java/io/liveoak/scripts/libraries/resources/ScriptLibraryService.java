package io.liveoak.scripts.libraries.resources;

import io.liveoak.scripts.libraries.manager.LibraryManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptLibraryService implements Service<ScriptLibraries> {

    ScriptLibraries scriptLibraries;

    @Override
    public void start(StartContext startContext) throws StartException {
        scriptLibraries = new ScriptLibraries(libraryManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        scriptLibraries = null;
    }

    @Override
    public ScriptLibraries getValue() throws IllegalStateException, IllegalArgumentException {
        return scriptLibraries;
    }

    public InjectedValue<LibraryManager> libraryManagerInjector = new InjectedValue<>();
}
