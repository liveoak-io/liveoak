package io.liveoak.container.tenancy.service;

import java.io.File;

import io.liveoak.common.util.FileHelper;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author Ken Finnigan
 */
public class ApplicationRemovalService implements Service<Void> {

    public ApplicationRemovalService(ServiceController<InternalApplication> applicationServiceController) {
        this.appServiceController = applicationServiceController;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        StabilityMonitor monitor = new StabilityMonitor();
        target.addMonitor(monitor);

        File appDir = this.appServiceController.getValue().directory();
        try {
            context.asynchronous();
            this.appServiceController.setMode(ServiceController.Mode.REMOVE);

            new Thread("ApplicationRemovalService worker thread") {
                public void run() {
                    try {
                        FileHelper.deleteNonEmpty(appDir.getAbsoluteFile());
                        context.complete();
                    } catch (Throwable e) {
                        context.failed(new StartException("Unable to remove application directory: " + appDir.getAbsolutePath(), e));
                    }
                }
            }.start();

            monitor.awaitStability();
            target.removeMonitor(monitor);
        } catch (Exception e) {
            throw new StartException(e);
        }

        // remove ourselves
        context.getController().setMode(ServiceController.Mode.REMOVE);
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() {
        return null;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertx;
    }

    private InjectedValue<Vertx> vertx = new InjectedValue<>();
    private ServiceController<InternalApplication> appServiceController;
}
