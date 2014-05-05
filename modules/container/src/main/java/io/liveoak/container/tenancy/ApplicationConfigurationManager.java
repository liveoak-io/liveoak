package io.liveoak.container.tenancy;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class ApplicationConfigurationManager {

    public ApplicationConfigurationManager(File file) {
        this.file = file;
    }

    public synchronized ObjectNode read() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        return (ObjectNode) mapper.readTree(this.file);
    }

    public synchronized void updateResource(String id, String type, ObjectNode config) throws IOException {
        ObjectNode tree = read();
        ObjectNode resourcesTree = (ObjectNode) tree.get("resources");
        if (resourcesTree == null) {
            resourcesTree = JsonNodeFactory.instance.objectNode();
            tree.put("resources", resourcesTree);
        }

        ObjectNode resourceTree = (ObjectNode) resourcesTree.get(id);
        if (resourceTree == null) {
            resourceTree = JsonNodeFactory.instance.objectNode();
            resourceTree.put("type", type);
            resourcesTree.put(id, resourceTree);
        }

        resourceTree.put("config", config);

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        writer.writeValue(this.file, tree);
    }

    public synchronized void removeResource(String id) throws IOException {
        ObjectNode tree = read();
        ObjectNode resourcesTree = (ObjectNode) tree.get("resources");
        if (resourcesTree == null) {
            return;
        }

        resourcesTree.remove( id );

        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        log.debug("writing " + tree + " to " + this.file);
        writer.writeValue( this.file, tree );
        log.debug("write complete");
    }

    private final File file;

    private static final Logger log = Logger.getLogger(ApplicationConfigurationManager.class);
}
