package io.liveoak.container.extension.system;


import org.jboss.logging.Logger;
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
public class ExtensionLoader implements Service<Void> {

    private static final Logger log = Logger.getLogger(ExtensionLoader.class);

    public ExtensionLoader(File configDir) {
        this.configDir = configDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if ( this.configDir != null && this.configDir.exists()) {
            context.asynchronous();
            new Thread(() -> {
                try {
                    File[] configs = this.configDir.listFiles(configFile -> !configFile.getName().startsWith(".") && configFile.getName().endsWith(".json"));
                    for (File config : configs) {
                        try {
                            this.extensionInstaller.getValue().load(config);
                        } catch (Exception e) {
                            // TODO: are we to ignore an exception here, and continue?
                            log.error("[IGNORED] Failed to load: " + config.getName(), e);
                        }
                    }
                } catch (Throwable t) {
                    context.failed(new StartException(t));
                }
                context.complete();
            }, "ExtensionLoader starter").start();
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<ExtensionInstaller> extensionInstallerInjector() {
        return this.extensionInstaller;
    }

    private File configDir;
    private InjectedValue<ExtensionInstaller> extensionInstaller = new InjectedValue<>();
}
