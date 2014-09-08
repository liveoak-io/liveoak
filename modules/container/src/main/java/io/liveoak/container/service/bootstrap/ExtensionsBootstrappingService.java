package io.liveoak.container.service.bootstrap;

import java.io.File;

import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.extension.ExtensionLoader;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.container.zero.service.ZeroBootstrapper;
import io.liveoak.spi.Services;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

import static io.liveoak.spi.Services.EXTENSION_INSTALLER;
import static io.liveoak.spi.Services.EXTENSION_LOADER;

/**
 * @author Bob McWhirter
 */
public class ExtensionsBootstrappingService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();


        ExtensionLoader extensionLoader = new ExtensionLoader(new File(this.extensionsDirectoryInjector.getValue()).getAbsoluteFile());

        target.addService(EXTENSION_LOADER, extensionLoader)
                .addDependency(EXTENSION_INSTALLER, ExtensionInstaller.class, extensionLoader.extensionInstallerInjector())
                .install();


        ExtensionInstaller installer = new ExtensionInstaller(target, Services.resource(ZeroExtension.APPLICATION_ID, "system"));
        target.addService(EXTENSION_INSTALLER, new ValueService<ExtensionInstaller>(new ImmediateValue<>(installer)))
                .install();

        ZeroBootstrapper zero = new ZeroBootstrapper();

        target.addService(Services.LIVEOAK.append("zero", "bootstrapper"), zero)
                .addDependency(EXTENSION_INSTALLER, ExtensionInstaller.class, zero.extensionInstallerInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<String> extensionsDirectoryInjector() {
        return this.extensionsDirectoryInjector;
    }

    private InjectedValue<String> extensionsDirectoryInjector = new InjectedValue<>();
}
