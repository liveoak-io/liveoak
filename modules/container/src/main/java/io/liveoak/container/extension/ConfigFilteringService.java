package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.liveoak.common.util.StringPropertyReplacer;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.util.Iterator;
import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class ConfigFilteringService implements Service<ObjectNode> {

    public ConfigFilteringService(Properties props) {
        this.props = new Properties( props );
        Properties sysProps = System.getProperties();
        for ( String name : sysProps.stringPropertyNames() ) {
            if ( name.startsWith( "io.liveoak" ) ) {
                this.props.setProperty( name, sysProps.getProperty( name ) );
            }
        }
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.config = filter(this.configurationInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.config = null;
    }

    @Override
    public ObjectNode getValue() throws IllegalStateException, IllegalArgumentException {
        return this.config;
    }

    protected ObjectNode filter(ObjectNode src) {
        ObjectNode dest = JsonNodeFactory.instance.objectNode();
        Iterator<String> names = src.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            dest.put(name, filter(src.get(name)));
        }
        return dest;
    }

    protected JsonNode filter(JsonNode src) {
        if (src instanceof TextNode) {
            return JsonNodeFactory.instance.textNode(filter(src.asText()));
        }
        if (src instanceof ObjectNode) {
            return filter((ObjectNode) src);
        }
        if (src instanceof ArrayNode) {
            ArrayNode dest = JsonNodeFactory.instance.arrayNode();

            Iterator<JsonNode> elements = dest.elements();
            dest.add(filter(elements.next()));
            return dest;
        }

        return src.deepCopy();
    }

    protected String filter(String src) {
        return StringPropertyReplacer.replaceProperties(src, this.props);
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private ObjectNode config;
    private Properties props;
}
