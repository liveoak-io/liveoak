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
import io.liveoak.scripts.resourcetriggered.resource.ScriptRegistry;
import io.liveoak.scripts.resourcetriggered.resource.ScriptRegistryService;
import io.liveoak.spi.Services;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptExtension implements Extension {

    public static final ServiceName SCRIPT_SERVICE_NAME = Services.LIVEOAK.append("scripts");
    public static final ServiceName RESOURCE_SCRIPT_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-script-manager");
    public static final ServiceName RESOURCE_SCRIPT_REGISTRY_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-registry");
    public static final ServiceName LIBRARY_MANAGER_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("library-manager");
    public static final ServiceName LIBRARIES_RESOURCE_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("libraries-resource");
    public static final ServiceName RESOURCE_SCRIPTS_SERVICE_NAME = SCRIPT_SERVICE_NAME.append("resource-scripts");

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
        ScriptRegistryService scriptRegistryService = new ScriptRegistryService();
        context.target().addService(RESOURCE_SCRIPT_REGISTRY_SERVICE_NAME.append(applicationId), scriptRegistryService)
                .install();


        ResourceScriptManagerService resourceInterceptorManagerService = new ResourceScriptManagerService();
        context.target().addService(RESOURCE_SCRIPT_MANAGER_SERVICE_NAME.append(applicationId), resourceInterceptorManagerService)
                .addDependency(LIBRARY_MANAGER_SERVICE_NAME.append(applicationId), LibraryManager.class, resourceInterceptorManagerService.getLibraryManagerInjector())
                .addDependency(SCRIPT_INTERCEPTOR_SERVICE_NAME, ScriptInterceptor.class, resourceInterceptorManagerService.getInterceptorInjector())
                .addDependency(RESOURCE_SCRIPT_REGISTRY_SERVICE_NAME.append(applicationId), ScriptRegistry.class, resourceInterceptorManagerService.getScriptRegistryInjector())
                .addDependency(Services.resource(context.application().id(), context.resourceId()).append("apply-config"))
                .addDependency(Services.resource(context.application().id(), context.resourceId()), ScriptsRootResource.class, resourceInterceptorManagerService.getScriptRootInjector())
                .addInjection(resourceInterceptorManagerService.getApplicationNameInjector(), applicationId)
                .install();


        ResourceScriptService resourceScriptsService = new ResourceScriptService(applicationId);
        context.target().addService(RESOURCE_SCRIPTS_SERVICE_NAME.append(applicationId), resourceScriptsService)
                .addDependency(RESOURCE_SCRIPT_REGISTRY_SERVICE_NAME.append(applicationId), ScriptRegistry.class, resourceScriptsService.getScriptRegistryInjector())
                .addDependency(Services.VERTX, Vertx.class, resourceScriptsService.getVertxInjector())
                .install();

        // ROOT SCRIPT RESOURCE
        ScriptService scriptsService = new ScriptService(context.resourceId());
        context.target().addService(Services.resource(context.application().id(), context.resourceId()), scriptsService)
                .addDependency(RESOURCE_SCRIPTS_SERVICE_NAME.append(applicationId), ResourceScripts.class, scriptsService.resourceScriptsInjector)
                .addDependency(LIBRARIES_RESOURCE_SERVICE_NAME.append(applicationId), ScriptLibraries.class, scriptsService.librariesResourceInjector)
                .install();
        context.mountPrivate(Services.resource(context.application().id(), context.resourceId()));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        //do nothing for now.
    }
}
