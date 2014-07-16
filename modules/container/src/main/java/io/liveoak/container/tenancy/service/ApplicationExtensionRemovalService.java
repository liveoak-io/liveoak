package io.liveoak.container.tenancy.service;

import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.container.extension.ApplicationExtensionContextImpl;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
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

        ApplicationExtensionContextImpl extensionContext = new ApplicationExtensionContextImpl(
                target,
                appExtension,
                null,
                null,
                JsonNodeFactory.instance.objectNode(),
                false);

        StabilityMonitor monitor = new StabilityMonitor();
        target.addMonitor(monitor);

        try {
            this.extensionInjector.getValue().unextend(extensionContext);
            context.complete();
            monitor.awaitStability();
        } catch (Exception e) {
            throw new StartException(e);
        }

        // remove ourselves
        context.getController().setMode(ServiceController.Mode.REMOVE);
        this.appExtensionServiceController.setMode(ServiceController.Mode.REMOVE);
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

    private final CountDownLatch latch = new CountDownLatch(1);

}
