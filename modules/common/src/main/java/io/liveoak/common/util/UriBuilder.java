package io.liveoak.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class UriBuilder {

    private StringBuilder root = new StringBuilder();
    private LinkedHashMap<String, String> params = new LinkedHashMap<>();

    private UriBuilder() {
    }

    public UriBuilder(URI uri) {
        root.append(uri.getRawPath());
    }

    public UriBuilder addParam(String key, Object value) {
        try {
            params.put(key, URLEncoder.encode(String.valueOf(value), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Assertion failed: utf-8 not supported");
        }
        return this;
    }

    public UriBuilder addParamIf(boolean condition, String key, Object value) {
        if (condition) {
            return addParam(key, value);
        }
        return this;
    }

    public URI build() throws URISyntaxException {
        StringBuilder result = new StringBuilder(root);
        if (params.size() > 0) {
            result.append("?");
            boolean first = true;
            for (String key : params.keySet()) {
                if (!first) {
                    result.append("&");
                }
                result.append(key).append("=").append(params.get(key));
                first = false;
            }
        }
        return new URI(result.toString());
    }

    public UriBuilder copy() {
        UriBuilder copy = new UriBuilder();
        copy.root = new StringBuilder(root);
        copy.params = new LinkedHashMap(params);
        return copy;
    }
}
