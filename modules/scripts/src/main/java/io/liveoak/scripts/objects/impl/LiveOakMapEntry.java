package io.liveoak.scripts.objects.impl;

import java.util.AbstractMap;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakMapEntry<K,V> extends AbstractMap.SimpleEntry {
    public LiveOakMapEntry(Object key, Object value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return this.getKey() + ": " + this.getValue();
    }
}
