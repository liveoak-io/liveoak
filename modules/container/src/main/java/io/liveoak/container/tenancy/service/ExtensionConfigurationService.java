package io.liveoak.container.tenancy.service;

import java.io.File;

import io.liveoak.container.tenancy.ExtensionConfigurationManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ExtensionConfigurationService implements Service<ExtensionConfigurationManager> {

    public ExtensionConfigurationService(String id, File configFile) {
        this.id = id;
        this.configFile = configFile;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.manager = new ExtensionConfigurationManager(id, configFile);
    }

    @Override
    public void stop(StopContext context) {
        this.manager = null;
    }

    @Override
    public ExtensionConfigurationManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.manager;
    }

    private String id;
    private final File configFile;
    private ExtensionConfigurationManager manager;
}
