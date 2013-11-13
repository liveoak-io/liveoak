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
