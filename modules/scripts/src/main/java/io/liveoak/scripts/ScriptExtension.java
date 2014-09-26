package io.liveoak.scripts;

import io.liveoak.interceptor.service.InterceptorRegistrationHelper;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.libraries.manager.LibraryManagerService;
import io.liveoak.scripts.libraries.resources.ScriptLibraries;
import io.liveoak.scripts.libraries.resources.ScriptLibraryService;
import io.liveoak.scripts.resource.ScriptsRootResource;
import io.liveoak.scripts.resourcetriggered.interceptor.ScriptInterceptor;
import io.liveoak.scripts.resourcetriggered.interceptor.ScriptInterceptorService;
import io.liveoak.scripts.resourcetriggered.manager.ResourceScriptManagerService;
import io.liveoak.scripts.resourcetriggered.resource.ResourceScriptService;
import io.liveoak.scripts.resourcetriggered.resource.ResourceScripts;
import io.liveoak.scripts.resourcetriggered.resource.ScriptMap;
import io.liveoak.scripts.resourcetriggered.resource.ScriptMapService;
import io.liveoak.scripts.scheduled.manager.ScheduleManager;
import io.liveoak.scripts.scheduled.manager.ScheduleManagerService;
import io.liveoak.scripts.scheduled.manager.ScheduledScriptManager;
import io.liveoak.scripts.scheduled.manager.ScheduledScriptManagerService;
import io.liveoak.scripts.scheduled.manager.SchedulerService;
import io.liveoak.scripts.scheduled.resource.ScheduledScriptsResource;
import io.liveoak.scripts.scheduled.resource.ScheduledScriptsService;
import io.liveoak.spi.Services;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.quartz.Scheduler;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptExtension implements Extension {

    public static final ServiceName SCRIPT_SERVICE_NAME = Services.LIVEOAK.append("scripts");
    public static final ServiceName RESOURCE_SCRIPT_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-script-manager");
    public static final ServiceName RESOURCE_SCRIPT_MAP_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-map");
    public static final ServiceName LIBRARY_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("library-manager");
    public static final ServiceName LIBRARIES_RESOURCE_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("libraries-resource");
    public static final ServiceName RESOURCE_SCRIPTS_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-scripts");
    public static final ServiceName SCHEDULED_SCRIPTS_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("scheduled-scripts");

    public static final ServiceName SCHEDULER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("scheduler");
    public static final ServiceName SCHEDULE_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("schedule-manager");
    public static final ServiceName SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("scheduled-script-manager");

    public static final ServiceName SCRIPT_INTERCEPTOR_SERVICE_NAME = Services.interceptor("script-interceptor");

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();

        // RESOURCE TRIGGERED SCRIPTS INTERCEPTOR
        // NOTE: interceptors needs to be added at the system level and cannot be currently added at the application level.
        ScriptInterceptorService serverScriptInterceptorService = new ScriptInterceptorService();
        ServiceController<ScriptInterceptor> serverScriptController = target.addService(SCRIPT_INTERCEPTOR_SERVICE_NAME, serverScriptInterceptorService)
                .install();
        // Install the interceptor.
        InterceptorRegistrationHelper.installInterceptor(target, serverScriptController);
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        String applicationId = context.application().id();
        // RESOURCE LIBRARIES
        LibraryManagerService libraryManagerService = new LibraryManagerService();
        context.target().addService(LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), libraryManagerService)
                .addDependency(Services.CLIENT, Client.class, libraryManagerService.clientInjector)
                .install();

        ScriptLibraryService scriptLibraryService = new ScriptLibraryService();
        context.target().addService(LIBRARIES_RESOURCE_SERVICE_NAME.append(applicationId), scriptLibraryService)
                .addDependency(LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), LibraryManager.class, scriptLibraryService.libraryManagerInjector)
                .install();


        // RESOURCE TRIGGERED SCRIPTS
        ScriptMapService scriptMapService = new ScriptMapService();
        context.target().addService(RESOURCE_SCRIPT_MAP_SERVICE_NAME.append(applicationId), scriptMapService)
                .install();


        ResourceScriptManagerService resourceInterceptorManagerService = new ResourceScriptManagerService();
        context.target().addService(RESOURCE_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), resourceInterceptorManagerService)
                .addDependency(LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), LibraryManager.class, resourceInterceptorManagerService.libraryManagerInjector)
                .addDependency(SCRIPT_INTERCEPTOR_SERVICE_NAME, ScriptInterceptor.class, resourceInterceptorManagerService.interceptorInjector)
                .addDependency(RESOURCE_SCRIPT_MAP_SERVICE_NAME.append(applicationId), ScriptMap.class, resourceInterceptorManagerService.scriptMapInjector)
                .addDependency(Services.resource(context.application().id(), context.resourceId()).append("apply-config"))
                .addDependency(Services.resource(context.application().id(), context.resourceId()), ScriptsRootResource.class,resourceInterceptorManagerService.scriptRootInjector)
                .addInjection(resourceInterceptorManagerService.applicationNameInjector, applicationId)
                .install();


        ResourceScriptService resourceScriptsService = new ResourceScriptService();
        context.target().addService(RESOURCE_SCRIPTS_SERVICE_NAME.append(applicationId), resourceScriptsService)
                .addDependency(RESOURCE_SCRIPT_MAP_SERVICE_NAME.append(applicationId), ScriptMap.class, resourceScriptsService.scriptMapInjector)
                .addDependency(Services.VERTX, Vertx.class, resourceScriptsService.vertxInjector)
                .install();


        // ENDPOINT SCRIPTS
        // TODO


        // SCHEDULED SCRIPTS
        SchedulerService schedulerService = new SchedulerService(applicationId);
        context.target().addService(SCHEDULER_SERVICE_NAME.append(applicationId), schedulerService)
                .install();

        ScheduledScriptManagerService scheduledScriptManagerService = new ScheduledScriptManagerService();
        context.target().addService(SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), scheduledScriptManagerService)
                .addDependency(LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), LibraryManager.class, scheduledScriptManagerService.libraryManagerInjector)
                .install();

        ScheduleManagerService schedulerManagerService = new ScheduleManagerService();
        context.target().addService(SCHEDULE_MANAGER_SERVICE_NAME.append(applicationId), schedulerManagerService)
                .addDependency(SCHEDULER_SERVICE_NAME.append(applicationId), Scheduler.class, schedulerManagerService.schedulerInjector)
                .addDependency(SCHEDULED_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), ScheduledScriptManager.class, schedulerManagerService.scheduledScriptManagerInjector)
                .install();

        ScheduledScriptsService scheduledScriptsService = new ScheduledScriptsService();
        context.target().addService(SCHEDULED_SCRIPTS_SERVICE_NAME.append(applicationId), scheduledScriptsService)
                .addDependency(Services.VERTX, Vertx.class, scheduledScriptsService.vertxInjector)
                .addDependency(SCHEDULE_MANAGER_SERVICE_NAME.append(applicationId), ScheduleManager.class, scheduledScriptsService.scheduleManagerInjector)
                .install();


        // ROOT SCRIPT RESOURCE
        ScriptService scriptsService = new ScriptService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), scriptsService)
                .addDependency(RESOURCE_SCRIPTS_SERVICE_NAME.append(applicationId), ResourceScripts.class, scriptsService.resourceScriptsInjector)
                .addDependency(LIBRARIES_RESOURCE_SERVICE_NAME.append(applicationId), ScriptLibraries.class, scriptsService.librariesResourceInjector)
                .addDependency(SCHEDULED_SCRIPTS_SERVICE_NAME.append(applicationId), ScheduledScriptsResource.class, scriptsService.scheduledScriptsInjectedValue)
                .install();
        context.mountPrivate(Services.resource(context.application().id(), context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        //do nothing for now.
    }
}
