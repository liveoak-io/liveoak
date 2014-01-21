package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.ApplicationExtensionContextImpl;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionRemovalService implements Service<Void> {



    public ApplicationExtensionRemovalService(ServiceController<InternalApplicationExtension> appExtensionServiceController) {
        this.appExtensionServiceController = appExtensionServiceController;
    }

    @Override
    public void start(StartContext context) throws StartException {

        ServiceTarget target = context.getChildTarget();

        InternalApplicationExtension appExtension = this.appExtensionServiceController.getValue();

        String orgId = appExtension.application().organization().id();
        String appId = appExtension.application().id();

        ApplicationExtensionContextImpl extensionContext = new ApplicationExtensionContextImpl(
                target,
                appExtension,
                appExtension.id(),
                null,
                null,
                null);

        StabilityMonitor monitor = new StabilityMonitor();
        target.addMonitor( monitor );

        try {
            this.extensionInjector.getValue().unextend(extensionContext);
            context.complete();
            monitor.awaitStability();
        } catch (Exception e) {
            throw new StartException(e);
        }

        context.getController().setMode(ServiceController.Mode.REMOVE );
        this.appExtensionServiceController.setMode( ServiceController.Mode.REMOVE );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<Extension> extensionInjector() {
        return this.extensionInjector;
    }

    private final ServiceController<InternalApplicationExtension> appExtensionServiceController;
    private final InjectedValue<Extension> extensionInjector = new InjectedValue<>();

}
