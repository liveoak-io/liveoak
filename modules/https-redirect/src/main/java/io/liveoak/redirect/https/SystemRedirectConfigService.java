package io.liveoak.redirect.https;

import io.liveoak.redirect.https.resource.SystemRedirectConfig;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SystemRedirectConfigService implements Service<SystemRedirectConfig> {

    SystemRedirectConfig redirectConfig;
    String id;

    public SystemRedirectConfigService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.redirectConfig = new SystemRedirectConfig(id);
    }

    @Override
    public void stop(StopContext context) {
        //do nothing for now
    }

    @Override
    public SystemRedirectConfig getValue() throws IllegalStateException, IllegalArgumentException {
        return redirectConfig;
    }
}
