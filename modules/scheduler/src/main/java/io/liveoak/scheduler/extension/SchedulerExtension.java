package io.liveoak.scheduler.extension;

import io.liveoak.scheduler.SchedulerServices;
import io.liveoak.scheduler.service.SchedulerAdminResourceService;
import io.liveoak.scheduler.service.SchedulerResourceService;
import io.liveoak.scheduler.service.SchedulerService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
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
        context.mountPrivate(new DefaultRootResource(context.id()));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        String appId = context.application().id();

        ServiceTarget target = context.target();
        ServiceName name = SchedulerServices.scheduler(appId, context.resourceId());
        SchedulerService scheduler = new SchedulerService(appId + "/" + context.resourceId());

        target.addService(name, scheduler)
                .install();

        SchedulerResourceService publicResource = new SchedulerResourceService(context.resourceId());

        target.addService(LiveOak.resource(appId, context.resourceId()), publicResource)
                .addDependency(LiveOak.NOTIFIER, Notifier.class, publicResource.notifierInjector())
                .addDependency(name, Scheduler.class, publicResource.schedulerInjector())
                .install();

        context.mountPublic();

        SchedulerAdminResourceService privateResource = new SchedulerAdminResourceService(context.resourceId());

        target.addService(LiveOak.adminResource(appId, context.resourceId()), privateResource)
                .addDependency(name, Scheduler.class, privateResource.schedulerInjector())
                .install();

        context.mountPrivate();
    }


    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
