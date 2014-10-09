/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common;

import io.liveoak.spi.ResourceParams;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultResourceParams implements ResourceParams {

    public static ResourceParams instance(Map<String, List<String>> params) {
        if (params == null || params.size() == 0) {
            return NONE;
        }
        return new DefaultResourceParams(params);
    }

    private final Map<String, List<String>> params;

    private DefaultResourceParams() {
        params = Collections.emptyMap();
    }

    private DefaultResourceParams(Map<String, List<String>> params) {
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> ent: params.entrySet()) {
            result.put(ent.getKey(), Collections.unmodifiableList(ent.getValue()));
        }

        this.params = Collections.unmodifiableMap(result);
    }

    public Collection<String> names() {
        return params.keySet();
    }

    public boolean contains(String name) {
        return value(name) != null;
    }

    public String value(String name) {
        List<String> values = params.get(name);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    public List<String> values(String name) {
        return params.get(name);
    }

    public int intValue(String name, int def) {
        String val = value(name);
        if (val == null) {
            return def;
        }
        return Integer.parseInt(val);
    }

    public String toString() {
        return "[ResourceParams: params=" + this.params + "]";
    }



    public static class Builder {

        private LinkedHashMap<String, List<String>> params = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder add(String name, String value) {
            List<String> values = params.get(name);
            if (values == null) {
                values = new LinkedList<>();
                params.put(name, values);
            }
            values.add(value);
            return this;
        }

        public ResourceParams build() {
            return new DefaultResourceParams(params);
        }
    }
}
