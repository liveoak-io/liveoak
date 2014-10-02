package io.liveoak.scripts;

import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.scheduled.manager.ScheduleManager;
import io.liveoak.scripts.scheduled.manager.ScheduleManagerService;
import io.liveoak.scripts.scheduled.manager.ScheduledScriptManager;
import io.liveoak.scripts.scheduled.manager.ScheduledScriptManagerService;
import io.liveoak.scripts.scheduled.manager.SchedulerService;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.quartz.Scheduler;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptExtension implements Extension {

    public static final ServiceName SCHEDULED_SCRIPTS_SERVICE_NAME = ScriptExtension.SCRIPT_SERVICE_NAME.append("scheduled-scripts");

    public static final ServiceName SCHEDULER_SERVICE_NAME = SCHEDULED_SCRIPTS_SERVICE_NAME.append("scheduler");
    public static final ServiceName SCHEDULE_MANAGER_SERVICE_NAME = SCHEDULED_SCRIPTS_SERVICE_NAME.append("schedule-manager");
    public static final ServiceName SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME = SCHEDULED_SCRIPTS_SERVICE_NAME.append("scheduled-script-manager");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        //do nothing for now
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        String applicationId = context.application().id();


        // SCHEDULED SCRIPTS
        SchedulerService schedulerService = new SchedulerService(applicationId);
        context.target().addService(SCHEDULER_SERVICE_NAME.append(applicationId), schedulerService)
                .install();

        ScheduledScriptManagerService scheduledScriptManagerService = new ScheduledScriptManagerService();
        context.target().addService(SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), scheduledScriptManagerService)
                .addDependency(ScriptExtension.LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), LibraryManager.class, scheduledScriptManagerService.libraryManagerInjector)
                .install();

        ScheduleManagerService schedulerManagerService = new ScheduleManagerService();
        context.target().addService(SCHEDULE_MANAGER_SERVICE_NAME.append(applicationId), schedulerManagerService)
                .addDependency(SCHEDULER_SERVICE_NAME.append(applicationId), Scheduler.class, schedulerManagerService.schedulerInjector)
                .addDependency(SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), ScheduledScriptManager.class, schedulerManagerService.scheduledScriptManagerInjector)
                .install();

        ScheduledScriptService scheduledScriptsService = new ScheduledScriptService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), scheduledScriptsService)
                .addDependency(Services.VERTX, Vertx.class, scheduledScriptsService.vertxInjector)
                .addDependency(SCHEDULE_MANAGER_SERVICE_NAME.append(applicationId), ScheduleManager.class, scheduledScriptsService.scheduleManagerInjector)
                .install();

        context.mountPrivate(Services.resource(context.application().id(), context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        //do nothing for now.
    }
}
