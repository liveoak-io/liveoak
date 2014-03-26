package io.liveoak.container.tenancy.service;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplicationRegistry;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class ApplicationsDeployerService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        File[] appDirs = this.appDirInjector.getValue().listFiles(pathname -> pathname.isDirectory());

        for(File appDir: appDirs) {
            if ((new File(appDir, "application.json")).exists()) {
                try {
                    this.registryInjector.getValue().createApplication(appDir.getName(), appDir.getName());
                } catch (InterruptedException e) {
                    throw new StartException(e);
                }
            }
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplicationRegistry> applicationRegistryInjector() {
        return this.registryInjector;
    }

    public Injector<File> applicationsDirectoryInjector() {
        return this.appDirInjector;
    }

    private InjectedValue<File> appDirInjector = new InjectedValue<>();
    private InjectedValue<InternalApplicationRegistry> registryInjector = new InjectedValue<>();
}
