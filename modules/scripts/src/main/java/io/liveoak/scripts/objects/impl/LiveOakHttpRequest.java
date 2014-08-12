package io.liveoak.scripts.objects.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakHttpRequest extends LiveOakMap<String, Object> {

    HttpRequest httpRequest;

    public LiveOakHttpRequest (HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<>();

        Entry entry = new LiveOakMapEntry<>("method", httpRequest.getMethod().name());
        entrySet.add(entry);

        entry = new LiveOakMapEntry<>("uri", httpRequest.getUri());
        entrySet.add(entry);

        entry = new LiveOakMapEntry<>("version", httpRequest.getProtocolVersion().text());
        entrySet.add(entry);

        Map<String, String> headers = new HashMap<>();
        for (Entry<String, String> headerEntry: httpRequest.headers().entries()) {
            headers.put(headerEntry.getKey(), headerEntry.getValue());
        }

        entry = new LiveOakMapEntry<>("headers", headers);
        entrySet.add(entry);

        return entrySet;
    }
}
