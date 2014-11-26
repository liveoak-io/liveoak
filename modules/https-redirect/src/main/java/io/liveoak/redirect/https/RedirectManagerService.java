package io.liveoak.redirect.https;

import io.liveoak.redirect.https.resource.SystemRedirectConfig;
import io.liveoak.spi.client.Client;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectManagerService implements Service<RedirectManager> {

    RedirectManager redirectManager;

    @Override
    public void start(StartContext context) throws StartException {
        redirectManager = new RedirectManager(systemRedirectServiceInjector.getValue(), clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        //do nothing for now
    }

    @Override
    public RedirectManager getValue() throws IllegalStateException, IllegalArgumentException {
        return redirectManager;
    }

    public InjectedValue<SystemRedirectConfig> systemRedirectServiceInjector = new InjectedValue<>();
    public InjectedValue<Client> clientInjector = new InjectedValue<>();
}
