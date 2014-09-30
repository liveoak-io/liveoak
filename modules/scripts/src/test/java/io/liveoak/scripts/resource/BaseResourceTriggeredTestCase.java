package io.liveoak.scripts.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.BaseScriptTestCase;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class BaseResourceTriggeredTestCase extends BaseScriptTestCase {
    public static final String RESOURCE_ID = "resource-triggered-scripts";
    public static final String RESOURCE_SCRIPT_PATH = SCRIPT_PATH + "/" + RESOURCE_ID;

    public static class MetadataState {
        String id;
        Map<String, Object> values = new HashMap<>();
        ArrayList<String> libraries = new ArrayList<>();

        public MetadataState(String id, String targetPath) {
            this.id = id;
            this.values.put("target-path", targetPath);
        }

        public MetadataState name(String name) {
            this.values.put("name", name);
            return this;
        }

        public MetadataState description(String description) {
            this.values.put("description", description);
            return this;
        }

        public MetadataState enabled(boolean enabled) {
            this.values.put("enabled", enabled);
            return this;
        }

        public MetadataState priority(int priority) {
            this.values.put("priority", priority);
            return this;
        }

        public MetadataState timeout(int timeout) {
            this.values.put("timeout", timeout);
            return this;
        }

        public MetadataState libraries(String... libraries) {
            for (String library:libraries) {
                this.libraries.add(library);
            }
            return this;
        }

        public ResourceState build() {
            ResourceState state = new DefaultResourceState(id);
            values.forEach((key, value) -> state.putProperty(key, value));
            state.putProperty("libraries", libraries);
            return state;
        }
    }

}
