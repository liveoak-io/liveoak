package io.liveoak.scripts.objects.impl;

import java.util.HashSet;
import java.util.Set;

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
        resourceState.putProperty(key, value);
        return resourceState.getProperty(key);
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
