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
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ExtensionConfigurationManager implements ConfigurationManager {

    private String type;
    private File file;
    private boolean versioned;
    private String versionedResourcePath;

    private static final Logger log = Logger.getLogger(ExtensionConfigurationManager.class);

    public ExtensionConfigurationManager(String type, File file) {
        this.type = type;
        this.file = file;
        this.versioned = false;
        this.versionedResourcePath = null;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String versionedResourcePath() {
        return versionedResourcePath;
    }

    @Override
    public boolean versioned() {
        return versioned;
    }

    @Override
    public void removeResource(String id, String type) throws Exception {
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
    public ObjectNode readResource(String id, String type) throws Exception {
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
