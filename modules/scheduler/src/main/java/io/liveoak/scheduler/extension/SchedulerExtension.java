package io.liveoak.scheduler.extension;

import io.liveoak.scheduler.SchedulerServices;
import io.liveoak.scheduler.service.SchedulerResourceService;
import io.liveoak.scheduler.service.SchedulerService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.Notifier;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.quartz.Scheduler;

/**
 * @author Bob McWhirter
 */
public class SchedulerExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();
        ServiceName name = SchedulerServices.scheduler(orgId, appId);
        SchedulerService scheduler = new SchedulerService(orgId + "/" + appId);

        target.addService(name, scheduler)
                .install();

        SchedulerResourceService resource = new SchedulerResourceService();

        target.addService(name.append("resource"), resource)
                .addDependency(LiveOak.NOTIFIER, Notifier.class, resource.notifierInjector())
                .addDependency(name, Scheduler.class, resource.schedulerInjector())
                .install();

        context.mountPublic(name.append("resource"));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
