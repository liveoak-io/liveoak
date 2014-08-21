package io.liveoak.scripts.objects.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakStateProperties extends LiveOakMap<String, Object> {

    ResourceState resourceState;

    public LiveOakStateProperties(ResourceState resourceState) {
        this.resourceState = resourceState;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();
        for (String propertyName : resourceState.getPropertyNames()) {
            Entry entry = new LiveOakMapEntry<>(propertyName, resourceState.getProperty(propertyName));
            entrySet.add(entry);
        }
        return entrySet;
    }

    @Override
    public Object put(String key, Object value) {

        convertToProperty(resourceState, key, value);
        return resourceState.getProperty(key);
    }

    public Object convertToProperty(ResourceState state, String key, Object value) {
        if (value instanceof Map) {
            ResourceState resourceState = new DefaultResourceState();
            Map<String, Object> map = (Map)value;
            for (String mapKey : map.keySet()) {
                convertToProperty(resourceState, mapKey, map.get(mapKey));
            }
            state.putProperty(key, resourceState);
        } else {
            state.putProperty(key, value);
        }

        return state;
    }

    @Override
    public Object get(Object key) {
        return resourceState.getProperty((String) key);
    }

    @Override
    public Object remove(Object key) {
        return resourceState.removeProperty((String) key);
    }
}
