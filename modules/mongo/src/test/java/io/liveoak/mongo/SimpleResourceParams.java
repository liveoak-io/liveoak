/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.spi.ResourceParams;

class SimpleResourceParams implements ResourceParams {

    Map<String, String> map = new HashMap<>();

    public void put(String name, String value) {
        map.put(name, value);
    }

    @Override
    public Collection<String> names() {
        return map.keySet();
    }

    @Override
    public boolean contains(String name) {
        return map.containsKey(name);
    }

    @Override
    public String value(String name) {
        return map.get(name);
    }

    @Override
    public List<String> values(String name) {
        List<String> list = new ArrayList<>();
        list.add(map.get(name));
        return list;
    }
}
