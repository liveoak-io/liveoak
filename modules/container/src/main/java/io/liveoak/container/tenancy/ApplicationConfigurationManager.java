package io.liveoak.container.tenancy;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.spi.ResourcePath;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ApplicationConfigurationManager {

    public ApplicationConfigurationManager(File file) {
        this.file = file;
    }

    public synchronized ObjectNode read() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        return (ObjectNode) mapper.readTree(this.file);
    }

    public synchronized void updateApplication(InternalApplication application) throws IOException {
        ObjectNode tree = read();
        tree.put("name", application.name());
        ResourcePath htmlAppPath = application.htmlApplicationResourcePath();
        if (htmlAppPath != null) {
            if (htmlAppPath.head().name().equals(application.id())) {
                htmlAppPath = htmlAppPath.subPath();
            }
            tree.put("html-app", htmlAppPath.toString());
        }
        tree.put("visible", application.visible());

        write(tree);
    }

    public ObjectNode readResource(String id) throws IOException {
        ObjectNode tree = read();
        ObjectNode resourcesTree = (ObjectNode) tree.get("resources");
        if (resourcesTree == null) {
            resourcesTree = JsonNodeFactory.instance.objectNode();
            tree.put("resources", resourcesTree);
        }

        return (ObjectNode) resourcesTree.get(id);
    }

    public synchronized void updateResource(String id, String type, JsonNode config) throws IOException {
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
        write(tree);
    }

    public synchronized void removeResource(String id) throws IOException {
        ObjectNode tree = read();
        ObjectNode resourcesTree = (ObjectNode) tree.get("resources");
        if (resourcesTree == null) {
            return;
        }

        resourcesTree.remove(id);
        write(tree);
    }

    private void write(ObjectNode tree) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        log.debug("writing " + tree + " to " + this.file);
        writer.writeValue(this.file, tree);
        log.debug("write complete");
    }

    private final File file;

    private static final Logger log = Logger.getLogger(ApplicationConfigurationManager.class);
}
