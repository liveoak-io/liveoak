package io.liveoak.scripts.objects.impl;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import io.liveoak.spi.ResourceParams;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceParameters extends AbstractMap<String, Object> {

    ResourceParams resourceParams;

    public LiveOakResourceParameters(ResourceParams resourceParams) {
        this.resourceParams = resourceParams;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();
        for (String paramName : resourceParams.names()) {
            Entry entry = new AbstractMap.SimpleEntry<>(paramName, resourceParams.value(paramName));
            entrySet.add(entry);
        }
        return entrySet;
    }
}
