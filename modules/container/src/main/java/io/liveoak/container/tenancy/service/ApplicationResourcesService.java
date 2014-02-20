package io.liveoak.container.tenancy.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Bob McWhirter
 */
public class ApplicationResourcesService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        File resourcesFile = new File( this.applicationInjector.getValue().directory(), "resources.json" );
        if ( ! resourcesFile.exists() ) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true );
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true );
        try {
            JsonNode tree = mapper.readTree(resourcesFile);
            Iterator<String> fields = tree.fieldNames();
            while ( fields.hasNext() ) {
                String resourceId = fields.next();
                JsonNode value = tree.get(resourceId);
                String extensionId = value.get( "type" ).asText();
                ObjectNode config = (ObjectNode) value.get( "config" );
                if ( config == null ) {
                    config = JsonNodeFactory.instance.objectNode();
                }
                this.applicationInjector.getValue().extend( extensionId, resourceId, config );
            }
        } catch (IOException e) {
            throw new StartException( e );
        } catch (InterruptedException e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
