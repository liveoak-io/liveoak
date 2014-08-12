package io.liveoak.scripts.objects.impl;

import java.util.HashSet;
import java.util.Set;

import io.liveoak.spi.RequestAttributes;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakRequestAttributes extends LiveOakMap<String, Object> {

    RequestAttributes attributes;

    public LiveOakRequestAttributes(RequestAttributes requestAttributes) {
        this.attributes = requestAttributes;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();
        for (String attributeName : attributes.getAttributeNames()) {
            Object attribute = attributes.getAttribute(attributeName);
            if (attribute instanceof HttpRequest) {
                LiveOakHttpRequest liveOakHttpRequest = new LiveOakHttpRequest((HttpRequest)attribute);
                Entry entry = new LiveOakMapEntry<>(attributeName, liveOakHttpRequest);
                entrySet.add(entry);
            } else {
                Entry entry = new LiveOakMapEntry<>(attributeName, attributes.getAttribute(attributeName));
                entrySet.add(entry);
            }
        }
        return entrySet;
    }
}