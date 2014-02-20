package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class ExtensionService implements Service<Extension> {

    public ExtensionService(String id, Extension extension, ObjectNode fullConfig) {
        this.id = id;
        this.extension = extension;
        this.fullConfig = fullConfig;
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

        SystemExtensionContext extContext = new SystemExtensionContextImpl(target, this.id, LiveOak.resource(ZeroExtension.APPLICATION_ID, "system"), extConfig );

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
    private boolean common;

}
