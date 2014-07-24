package io.liveoak.scripts.libraries.manager;

import io.liveoak.spi.client.Client;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LibraryManagerService implements Service<LibraryManager> {

    LibraryManager libraryManager;

    @Override
    public void start(StartContext startContext) throws StartException {
        libraryManager = new LibraryManager(clientInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        libraryManager = null;
    }

    @Override
    public LibraryManager getValue() throws IllegalStateException, IllegalArgumentException {
        return libraryManager;
    }

    public InjectedValue<Client> clientInjector = new InjectedValue<>();

}
