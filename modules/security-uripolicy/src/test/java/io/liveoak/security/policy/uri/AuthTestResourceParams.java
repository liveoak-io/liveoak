/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.spi.ResourceParams;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fork of DefaultResourceParams from container module
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AuthTestResourceParams implements ResourceParams {

    public static ResourceParams instance(Map<String, List<String>> params) {
        if (params == null || params.size() == 0) {
            return NONE;
        }
        return new AuthTestResourceParams(params);
    }

    private final Map<String, List<String>> params;

    private AuthTestResourceParams() {
        params = Collections.emptyMap();
    }

    private AuthTestResourceParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Collection<String> names() {
        return Collections.unmodifiableCollection(params.keySet());
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
        return Collections.unmodifiableList(params.get(name));
    }

    public int intValue(String name, int def) {
        String val = value(name);
        if (val == null) {
            return def;
        }
        return Integer.parseInt(val);
    }
}
