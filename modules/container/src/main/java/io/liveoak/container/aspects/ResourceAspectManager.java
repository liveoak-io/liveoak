/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.aspects;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class ResourceAspectManager {

    public ResourceAspectManager() {

    }

    public void put(String name, ResourceAspect aspect) {
        this.aspects.put( name, aspect );
    }

    public ResourceAspect get(String name) {
        return this.aspects.get( name );
    }

    public boolean contains(String name) {
        return this.aspects.containsKey( name );
    }

    public Stream<ResourceAspect> stream() {
        return this.aspects.values().stream();
    }

    private Map<String, ResourceAspect> aspects = new HashMap<>();
}
