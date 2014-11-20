package io.liveoak.container.extension;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.container.tenancy.ConfigurationManager;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ExtensionConfigurationManager implements ConfigurationManager {

    private String extensionId;
    private File file;

    private static final Logger log = Logger.getLogger(ExtensionConfigurationManager.class);

    public ExtensionConfigurationManager(String extensionId, File file) {
        this.extensionId = extensionId;
        this.file = file;
    }

    public String extensionId() {
        return extensionId;
    }

    @Override
    public void removeResource(String id) throws Exception {
        if (file != null) {
            ObjectNode tree = read();

            if (id.equals("module")) {
                throw new Exception("Cannot delete main module resource");
            } else {
                ObjectNode instances = (ObjectNode) tree.get("instances");

                if (instances == null) {
                    return;
                }

                instances.remove(id);
                write(tree);
            }
        }
    }

    @Override
    public void updateResource(String id, String type, JsonNode config) throws Exception {
        if (file != null) {
            ObjectNode tree = read();

            ((ObjectNode) config).remove("id");

            if (id.equals("module")) {
                tree.put("config", config);
            } else {
                ObjectNode instances = (ObjectNode) tree.get("instances");
                if (instances == null) {
                    instances = JsonNodeFactory.instance.objectNode();
                    tree.put("instances", instances);
                }

                instances.put(id, config);
            }
            write(tree);
        }
    }

    @Override
    public ObjectNode readResource(String id) throws Exception {
        if (file != null) {
            ObjectNode tree = read();

            if (id.equals("module")) {
                return (ObjectNode) tree.get("config");
            } else {
                return (ObjectNode) tree.get("instances").get(id);
            }
        } else {
            return JsonNodeFactory.instance.objectNode();
        }
    }

    public void createResource(String id, String type, JsonNode config) throws Exception {
        if (file != null) {
            ObjectNode tree = read();


            if (id.equals("module")) {
                throw new Exception("Cannot create a main module resource over REST");
            } else {
                ObjectNode instances = (ObjectNode) tree.get("instances");
                if (instances == null) {
                    instances = JsonNodeFactory.instance.objectNode();
                    tree.put("instances", instances);
                }

                ((ObjectNode) config).remove("id");
                instances.put(id, config);
            }

            write(tree);
        }
    }


    private synchronized ObjectNode read() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        return (ObjectNode) mapper.readTree(this.file);
    }

    private synchronized void write(ObjectNode tree) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        log.debug("writing " + tree + " to " + this.file);
        writer.writeValue(this.file, tree);
        log.debug("write complete");
    }
}
