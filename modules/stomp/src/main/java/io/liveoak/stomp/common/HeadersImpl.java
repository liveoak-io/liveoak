
/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.common;

import io.liveoak.stomp.Headers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HeadersImpl extends HashMap<String, String> implements Headers {

    private static final long serialVersionUID = 1L;

    @Override
    public String get(String headerName) {
        return super.get(headerName);
    }

    public void putAll(Headers headers) {
        for (String name : headers.getHeaderNames()) {
            put(name, headers.get(name));
        }
    }

    public void remove(String headerName) {
        super.remove(headerName);
    }

    @Override
    public Set<String> getHeaderNames() {
        return keySet();
    }

    @Override
    public Headers duplicate() {
        HeadersImpl dupe = new HeadersImpl();
        dupe.putAll((Map<String, String>) this);
        return dupe;
    }

}
