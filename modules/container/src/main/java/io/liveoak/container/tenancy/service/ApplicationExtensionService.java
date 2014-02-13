package io.liveoak.container.tenancy.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.extension.ApplicationExtensionContextImpl;
import io.liveoak.container.extension.ConfigFilteringService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionService implements Service<InternalApplicationExtension> {


    public ApplicationExtensionService(String extensionId, ObjectNode configuration) {
        this.extensionId = extensionId;
        this.configuration = configuration;
    }

    @Override
    public void start(StartContext context) throws StartException {

        System.err.println( "** Activate " + this.extensionId + " for " + this.applicationInjector.getValue().id() );
        ServiceTarget target = context.getChildTarget();

        this.appExtension = new InternalApplicationExtension(
                this.serviceRegistryInjector.getValue(),
                this.applicationInjector.getValue(),
                this.extensionId
        );

        ServiceName configName = context.getController().getName().append( "config" );
        target.addService( configName, new ValueService<ObjectNode>( new ImmediateValue<>( this.configuration ) ) )
                .install();

        ServiceName filteredConfigName = configName.append( "filtered" );
        ConfigFilteringService filteredConfig = new ConfigFilteringService( properties() );
        target.addService( filteredConfigName, filteredConfig )
                .addDependency( configName, ObjectNode.class, filteredConfig.configurationInjector() )
                .install();

        String orgId = this.appExtension.application().organization().id();
        String appId = this.appExtension.application().id();

        ServiceName adminMountName = LiveOak.applicationExtensionAdminResource(orgId, appId, this.extensionId );

        this.extensionContext = new ApplicationExtensionContextImpl(
                context.getChildTarget(),
                this.appExtension,
                this.appExtension.id(),
                filteredConfigName,
                LiveOak.applicationContext(orgId, appId),
                adminMountName );

        try {
            this.extensionInjector.getValue().extend(extensionContext);
        } catch (Exception e) {
            throw new StartException(e);
        }

        ApplicationExtensionResourceService resource = new ApplicationExtensionResourceService( this.appExtension, configName );

        context.getChildTarget().addService( adminMountName, resource )
                .addDependency(LiveOak.applicationAdminResource(orgId, appId), ApplicationResource.class, resource.applicationResourceInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public InternalApplicationExtension getValue() throws IllegalStateException, IllegalArgumentException {
        return this.appExtension;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    public Injector<ServiceRegistry> serviceRegistryInjector() {
        return this.serviceRegistryInjector;
    }

    public Injector<ServiceContainer> serviceContainerInjector() {
        return this.serviceContainerInjector;
    }

    public Injector<Extension> extensionInjector() {
        return this.extensionInjector;
    }

    protected Properties properties() {
        InternalApplication app = this.applicationInjector.getValue();
        InternalOrganization org = app.organization();

        Properties props = new Properties();

        props.put( "organization.name", org.name() );
        props.put( "organization.id", org.id() );
        props.put( "organization.url",  "/" + org.id() );

        props.put( "application.name", app.name() );
        props.put( "application.id", app.id() );
        props.put( "application.url",  "/" + org.id() + "/" + app.id() );

        props.put( "application.dir", app.directory().getAbsolutePath() );

        return props;
    }


    private String extensionId;
    private boolean remove;

    private ApplicationExtensionContextImpl extensionContext;

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
    private InjectedValue<ServiceRegistry> serviceRegistryInjector = new InjectedValue<>();
    private InjectedValue<ServiceContainer> serviceContainerInjector = new InjectedValue<>();
    private InjectedValue<Extension> extensionInjector = new InjectedValue<>();

    private InternalApplicationExtension appExtension;
    private final ObjectNode configuration;
}
