package io.liveoak.scripts.objects.impl;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import io.liveoak.spi.RequestAttributes;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakRequestAttributes extends AbstractMap<String, Object> {

    RequestAttributes attributes;

    public LiveOakRequestAttributes(RequestAttributes requestAttributes) {
        this.attributes = requestAttributes;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();
        for (String attributeName : attributes.getAttributeNames()) {
            Entry entry = new AbstractMap.SimpleEntry<>(attributeName, attributes.getAttribute(attributeName));
            entrySet.add(entry);
        }
        return entrySet;
    }
}