package io.liveoak.redirect.https;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectInterceptorService implements Service<RedirectInterceptor> {

    private RedirectInterceptor redirector;

    @Override
    public void start(StartContext context) throws StartException {
        redirector = new RedirectInterceptor(redirectManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public RedirectInterceptor getValue() throws IllegalStateException, IllegalArgumentException {
        return redirector;
    }

    public InjectedValue<RedirectManager> redirectManagerInjector = new InjectedValue<>();
}
