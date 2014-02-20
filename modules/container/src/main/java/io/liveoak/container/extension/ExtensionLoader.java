package io.liveoak.container.extension;


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

    public ExtensionLoader(File configDir) {
        this.configDir = configDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if ( this.configDir != null && this.configDir.exists()) {
            File[] configs = this.configDir.listFiles();
            for ( File config : configs ) {
                try {
                    this.extensionInstaller.getValue().load( config );
                } catch (Exception e) {
                    e.printStackTrace();
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

    public Injector<ExtensionInstaller> extensionInstallerInjector() {
        return this.extensionInstaller;
    }

    private File configDir;
    private InjectedValue<ExtensionInstaller> extensionInstaller = new InjectedValue<>();
}
