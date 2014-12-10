package io.liveoak.spi.resource.config;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.util.ObjectMapperFactory;

/**
 * @author Ken Finnigan
 */
public interface ConfigResource {
    default boolean hasOwnConfigFile() {
        return false;
    }

    default File configFile(Resource resource, File configRoot) {
        return configRoot;
    }

    default void persistConfig(Resource resource, ObjectNode config, File configRoot) throws IOException {
        File configFile = configFile(resource, configRoot);
        config.put(LiveOak.ID, resource.id());

        if (hasOwnConfigFile()) {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            write(configFile, config);
        } else {
            JsonNode tree = read(configFile);
            if (tree.isArray()) {
                int index = findExisting((ArrayNode) tree, resource.id());

                if (index >= 0) {
                    ((ArrayNode) tree).set(index, config);
                } else {
                    ((ArrayNode) tree).add(config);
                }

                write(configFile, tree);
            }
        }
    }

    default void removeConfig(Resource resource, File configRoot) throws IOException {
        File configFile = configFile(resource, configRoot);

        if (hasOwnConfigFile()) {
            // TODO Delete the file
        } else {
            JsonNode tree = read(configFile);
            if (tree.isArray()) {
                int index = findExisting((ArrayNode) tree, resource.id());

                if (index != -1) {
                    ((ArrayNode) tree).remove(index);
                    write(configFile, tree);
                }
            }
        }
    }

    default int findExisting(ArrayNode array, String resourceId) {
        Iterator<JsonNode> nodes = array.elements();
        int count = 0;
        boolean exists = false;
        while (nodes.hasNext()) {
            JsonNode node = nodes.next();
            if (node.get(LiveOak.ID).asText().equals(resourceId)) {
                exists = true;
                break;
            }
            count++;
        }
        return exists ? count : -1;
    }

    default void write(File configFile, JsonNode tree) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        writer.writeValue(configFile, tree);
    }

    default JsonNode read(File configFile) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        if (!configFile.exists()) {
            write(configFile, JsonNodeFactory.instance.arrayNode());
        }
        return mapper.readTree(configFile);
    }

}
