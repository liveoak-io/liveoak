package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ExtensionService implements Service<Extension> {

    public ExtensionService(String id, Extension extension, ObjectNode fullConfig, ServiceName systemConfigMount) {
        this.id = id;
        this.extension = extension;
        this.fullConfig = fullConfig;
        this.systemConfigMount = systemConfigMount;
        this.common = false;
        if ( fullConfig.has( "common" ) ) {
            this.common = fullConfig.get( "common" ).asBoolean();
        }
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ServiceName name = LiveOak.extension( this.id );

        ObjectNode extConfig = (ObjectNode) this.fullConfig.get( "config" );
        if ( extConfig == null ) {
            extConfig = JsonNodeFactory.instance.objectNode();
        }

        target.addService(name.append("config"), new ValueService<ObjectNode>(new ImmediateValue<>(extConfig)))
                .install();

        ExtensionConfigResourceService configResource = new ExtensionConfigResourceService( this.id, this.systemConfigMount, name.append( "config" ) );

        target.addService( LiveOak.extension(this.id).append( "admin" ), configResource )
                .addDependency( LiveOak.SERVICE_CONTAINER, ServiceContainer.class, configResource.serviceContainerInjector() )
                .install();

        SystemExtensionContext extContext = new SystemExtensionContextImpl(target, this.id, LiveOak.extension(this.id).append("config"));

        try {
            this.extension.extend(extContext);
        } catch (Exception e) {
            throw new StartException(e);
        }
        System.err.println( "** Extension activated: " + this.id );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Extension getValue() throws IllegalStateException, IllegalArgumentException {
        return this.extension;
    }

    String id() {
        return this.id;
    }

    boolean common() {
        return this.common;
    }

    ObjectNode applicationConfiguration() {
        ObjectNode appConfig = (ObjectNode) this.fullConfig.get( "app-config" );
        if ( appConfig != null ) {
            return appConfig;
        }

        return JsonNodeFactory.instance.objectNode();
    }


    private final String id;
    private final Extension extension;
    private final ObjectNode fullConfig;
    private final ServiceName systemConfigMount;
    private boolean common;

}
