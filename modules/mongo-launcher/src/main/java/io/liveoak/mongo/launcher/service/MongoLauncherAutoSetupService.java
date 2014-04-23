package io.liveoak.mongo.launcher.service;

import io.liveoak.mongo.launcher.MongoLauncherAutoSetup;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class MongoLauncherAutoSetupService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        context.asynchronous();

        new Thread() {
            @Override
            public void run() {
                try {
                    MongoLauncherAutoSetup.setup(
                            new File(liveoakDirInjector.getValue()).getAbsoluteFile(),
                            new File(extensionsDirInjector.getValue()).getAbsoluteFile());
                } catch (Throwable t) {
                    context.failed(new StartException(t));
                    return;
                }
                context.complete();
            }
        }.start();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<String> liveoakDirInjector() {
        return this.liveoakDirInjector;
    }

    public Injector<String> extensionsDirInjector() {
        return this.extensionsDirInjector;
    }

    private InjectedValue<String> liveoakDirInjector = new InjectedValue<>();
    private InjectedValue<String> extensionsDirInjector = new InjectedValue<>();
}
