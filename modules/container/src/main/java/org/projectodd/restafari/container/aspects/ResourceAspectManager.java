package org.projectodd.restafari.container.aspects;

import java.util.HashMap;
import java.util.Map;

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

    private Map<String, ResourceAspect> aspects = new HashMap<>();
}
