package io.liveoak.container.tenancy.service;

import java.io.File;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class ApplicationConfigurationService implements Service<ApplicationConfigurationManager> {

    public ApplicationConfigurationService(File configFile) {
        this.configFile = configFile;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.manager = new ApplicationConfigurationManager(this.configFile);
    }

    @Override
    public void stop(StopContext context) {
        this.manager = null;
    }

    @Override
    public ApplicationConfigurationManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.manager;
    }

    private final File configFile;
    private ApplicationConfigurationManager manager;

}
