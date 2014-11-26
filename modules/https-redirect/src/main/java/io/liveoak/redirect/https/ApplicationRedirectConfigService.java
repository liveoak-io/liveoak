package io.liveoak.redirect.https;

import io.liveoak.redirect.https.resource.ApplicationRedirectConfig;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ApplicationRedirectConfigService implements Service<ApplicationRedirectConfig> {

    ApplicationRedirectConfig applicationRedirectConfig;
    String resourceId;
    String applicationId;

    ApplicationRedirectConfigService(String applicationId, String resourceId) {
        this.applicationId = applicationId;
        this.resourceId = resourceId;
    }

    @Override
    public void start(StartContext context) throws StartException {
        applicationRedirectConfig = new ApplicationRedirectConfig(resourceId);
        redirectManagerInjector.getValue().addRedirect(applicationId, applicationRedirectConfig);
    }

    @Override
    public void stop(StopContext context) {
        redirectManagerInjector.getValue().removeRedirect(applicationId);
    }

    @Override
    public ApplicationRedirectConfig getValue() throws IllegalStateException, IllegalArgumentException {
        return applicationRedirectConfig;
    }

    public InjectedValue<RedirectManager> redirectManagerInjector = new InjectedValue<>();
}
