package io.liveoak.container.tenancy.service;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.ApplicationContext;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
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
        }

        if ( ! appDir.exists() ) {
            System.err.println( "attempt to create: " + appDir );
            appDir.mkdirs();
        }

        if ( ! appDir.exists() ) {
            System.err.println( "FAILED TO CREATE: " + appDir );
        }

        System.err.println(appDir + " .mkdirs: " + appDir.mkdirs() + " // " + appDir.exists());
        File applicationJson = new File(appDir, "application.json");

        String appName = this.id;
        ResourcePath htmlApp = null;
        ResourceState resourcesTree = null;

        if (applicationJson.exists()) {
            JSONDecoder decoder = new JSONDecoder();
            try {
                ResourceState state = decoder.decode(applicationJson);
                Object value;
                if ((value = state.getProperty("name")) != null) {
                    appName = (String) value;
                }
                if ((value = state.getProperty("html-app")) != null) {
                    htmlApp = new ResourcePath((String) value);
                    htmlApp.prependSegment(this.id);
                }
                if ((value = state.getProperty("resources")) != null) {
                    resourcesTree = (ResourceState) value;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ObjectMapper mapper = ObjectMapperFactory.create();
            ObjectWriter writer = mapper.writer().with(new DefaultPrettyPrinter("\n"));
            ObjectNode tree = JsonNodeFactory.instance.objectNode();
            tree.put("id", this.id);
            tree.put("name", appName);
            tree.put("resources", JsonNodeFactory.instance.objectNode());
            try {
                applicationJson.getParentFile().mkdirs();
                writer.writeValue(applicationJson, tree);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        ServiceName configManagerName = LiveOak.applicationConfigurationManager(this.id);
        ApplicationConfigurationService configManager = new ApplicationConfigurationService(applicationJson);
        target.addService(configManagerName, configManager)
                .install();

        ApplicationResourcesService resources = new ApplicationResourcesService(resourcesTree);

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
