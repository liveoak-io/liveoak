package io.liveoak.container.tenancy.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.ApplicationContext;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ResourcePath;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class ApplicationService implements Service<InternalApplication> {

    public ApplicationService(String id, String name, File directory) {
        this.id = id;
        this.name = name;
        this.directory = directory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        File appDir = this.directory;

        if (appDir == null) {
            appDir = new File(this.applicationsDirectoryInjector.getValue(), this.id);
            appDir.mkdirs();
        }

        File applicationJson = new File(appDir, "application.json");

        String appName = this.id;
        ResourcePath htmlApp = null;

        if (applicationJson.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            try {
                JsonNode tree = mapper.readTree(applicationJson);
                System.err.println( "TREE: " + tree );
                if ( tree.has( "name" ) ) {
                    appName = tree.get( "name" ).asText();
                }
                if ( tree.has( "html-app" ) ) {
                    htmlApp = new ResourcePath( tree.get( "html-app" ).asText() );
                    htmlApp.prependSegment( this.id );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.err.println( "application.json says: " + htmlApp );

        this.app = new InternalApplication(target, this.id, appName, appDir, htmlApp);

        // context resource

        ServiceName appContextName = LiveOak.applicationContext(this.id);
        ApplicationContextService appContext = new ApplicationContextService(this.app);
        target.addService(appContextName, appContext)
                .install();
        MountService<ApplicationContext> appContextMount = new MountService<>();
        this.app.contextController(target.addService(appContextName.append("mount"), appContextMount)
                .addDependency(LiveOak.GLOBAL_CONTEXT, MountPointResource.class, appContextMount.mountPointInjector())
                .addDependency(appContextName, ApplicationContext.class, appContextMount.resourceInjector())
                .install());

        // admin resource

        ServiceName appResourceName = LiveOak.applicationAdminResource(this.id);
        ApplicationResourceService appResource = new ApplicationResourceService(this.app);
        target.addService(appResourceName, appResource)
                .install();
        MountService<ApplicationResource> appResourceMount = new MountService<>();
        this.app.resourceController(target.addService(appResourceName.append("mount"), appResourceMount)
                .addDependency(LiveOak.resource(ZeroExtension.APPLICATION_ID, "applications"), MountPointResource.class, appResourceMount.mountPointInjector())
                .addDependency(appResourceName, ApplicationResource.class, appResourceMount.resourceInjector())
                .install());

        ApplicationResourcesService resources = new ApplicationResourcesService();

        target.addService(LiveOak.application(this.id).append("resources"), resources)
                .addInjectionValue(resources.applicationInjector(), this)
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.app = null;
    }

    @Override
    public InternalApplication getValue() throws IllegalStateException, IllegalArgumentException {
        return this.app;
    }

    public Injector<File> applicationsDirectoryInjector() {
        return this.applicationsDirectoryInjector;
    }

    private String id;
    private String name;
    private File directory;
    private InjectedValue<File> applicationsDirectoryInjector = new InjectedValue<>();
    private InternalApplication app;
}
