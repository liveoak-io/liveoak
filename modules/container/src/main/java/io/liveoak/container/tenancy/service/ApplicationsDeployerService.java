package io.liveoak.container.tenancy.service;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplicationRegistry;
import org.jboss.logging.Logger;
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

    private static final Logger log = Logger.getLogger(ApplicationsDeployerService.class);

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        new Thread(() -> {
            try {
                File[] appDirs = this.appDirInjector.getValue().listFiles(pathname -> pathname.isDirectory());

                for (File appDir : appDirs) {
                    if ((new File(appDir, "application.json")).exists()) {
                        try {
                            this.registryInjector.getValue().createApplication(appDir.getName(), appDir.getName(), appDir);
                        } catch (InterruptedException e) {
                            context.failed(new StartException(e));
                            return;
                        } catch (Throwable t) {
                            log.error("[IGNORED] Failed to deploy application: " + appDir.getName(), t);
                        }
                    }
                }
                context.complete();

            } catch (Throwable th) {
                context.failed(new StartException(th));
            }
        }, "ApplicationsDeployerService starter").start();
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
