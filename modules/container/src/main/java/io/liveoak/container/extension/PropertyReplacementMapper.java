package io.liveoak.container.extension;

import java.util.List;
import java.util.stream.Collectors;

import io.liveoak.common.util.ObjectsTree;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public interface PropertyReplacementMapper {

    default void readConfigEnvVars(ObjectsTree<Object> configVars, RequestContext ctx, PropertySink sink) {
        boolean runtimeValuePresent = ctx.resourceParams().value("runtime") != null;
        boolean runtimeRequested = runtimeValuePresent ? Boolean.parseBoolean(ctx.resourceParams().value("runtime")) : ctx.resourceParams().names().contains("runtime");

        // If runtime is not set, we replace the config values
        if (!runtimeRequested && configVars != null) {
            sink.replaceConfig((names, object) -> {
                ResourcePath path = new ResourcePath(names);
                List<Object> values = configVars.objects(path).collect(Collectors.toList());
                if (values != null && values.size() == 1) {
                    return values.get(0);
                }
                return object;
            });
        }
    }

    default ObjectsTree<Object> storeConfigEnvVars(ResourceState state) {
        ObjectsTree<Object> vars = new ObjectsTree<>();
        updateConfigEnvVars(vars, state);
        return vars;
    }

    default void updateConfigEnvVars(ObjectsTree<Object> configVars, ResourceState state, String... path) {
        state.getPropertyNames().forEach(name -> handleConfigObject(configVars, name, state.getProperty(name), path));
    }

    default void handleConfigObject(ObjectsTree<Object> configVars, String name, Object value, String... path) {
        if (value != null) {
            if (value instanceof ResourceState) {
                updateConfigEnvVars(configVars, (ResourceState) value, append(name, path));
            } else if (value instanceof List) {
                ((List) value).forEach(obj->handleConfigObject(configVars, name, obj, path));
            } else {
                String val = value.toString();
                int start = val.indexOf("${");
                int end = val.indexOf("}", start);
                if (end > start) {
                    configVars.addObject(value, new ResourcePath(append(name, path)));
                }
            }
        }
    }

    default String[] append(String first, String... path) {
        int newLength = path != null ? path.length + 1 : 1;
        String[] newArray = new String[newLength];
        newArray[0] = first;
        if (newLength != 1) {
            System.arraycopy(path, 0, newArray, 1, newLength - 1);
        }
        return newArray;
    }
}
