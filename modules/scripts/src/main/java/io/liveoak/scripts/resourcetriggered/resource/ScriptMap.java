package io.liveoak.scripts.resourcetriggered.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.liveoak.spi.ResourcePath;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptMap {

    Map<String, ResourceTriggeredScript> idMap = new HashMap<String, ResourceTriggeredScript>();
    PathMap pathMap = new PathMap();

    public void add(ResourceTriggeredScript script) {
        String id = script.getId();
        idMap.put(id, script);
        pathMap.add(script.getTarget(), id);
    }

    public void remove(String id) {
        ResourceTriggeredScript script = idMap.get(id);
        if (script != null) {
            idMap.remove(id);
            pathMap.remove(script.getTarget(), id);
        }
    }

    public int size() {
        return idMap.size();
    }

    public Collection<ResourceTriggeredScript> values() {
        return idMap.values();
    }

    public ResourceTriggeredScript get(String id) {
        return idMap.get(id);
    }

    public Set<ResourceTriggeredScript> getByPath(String path) {

        Set<ResourceTriggeredScript> scripts = new TreeSet<>(new ResourceTriggeredScript.PriorityComparator());

        Set<String> ids = pathMap.get(path);
        if (ids != null) {
            for (String id: ids) {
                scripts.add(idMap.get(id));
            }
        }

        return scripts;
    }

    public Set<ResourceTriggeredScript> getByPath(String path, ResourceTriggeredScript.FUNCTIONS function, Boolean enabled ) {
        Set<ResourceTriggeredScript> scripts = new TreeSet<>(new ResourceTriggeredScript.PriorityComparator());

        Set<String> ids = pathMap.get(path);

        if (ids != null) {
            for (String id: ids) {
                ResourceTriggeredScript script = idMap.get(id);
                if ((script.isEnabled() == enabled) && script.getProvides().contains(function)) {
                    scripts.add(idMap.get(id));
                }
            }
        }

        return scripts;
    }

    public Set<ResourceTriggeredScript> getByTarget(String target) {
        Set<ResourceTriggeredScript> scripts = new TreeSet<>(new ResourceTriggeredScript.PriorityComparator());
        for (String path: generatePaths(target)) {
            scripts.addAll(getByPath(path));
        }
        return scripts;
    }

    public Set<ResourceTriggeredScript> getByTarget(String target, ResourceTriggeredScript.FUNCTIONS function, Boolean enabled) {
        Set<ResourceTriggeredScript> scripts = new TreeSet<>(new ResourceTriggeredScript.PriorityComparator());
        for (String path: generatePaths(target)) {
            scripts.addAll(getByPath(path, function, enabled));
        }
        return scripts;
    }

    protected List<String> generatePaths(String uri) {
        List<String> paths = new ArrayList<>();

        ResourcePath resourcePath = new ResourcePath(uri);
        paths.add(resourcePath.toString());

        if (!resourcePath.segments().isEmpty()) {
            resourcePath = resourcePath.parent();
            if (!uri.endsWith("/*")) {
                paths.add(resourcePath.toString() + "/*");
                paths.add(resourcePath.toString() + "/**");
            }
        }
        while (!resourcePath.segments().isEmpty()) {
            resourcePath = resourcePath.parent();
            paths.add(resourcePath.toString() + "/**");
        }
        return paths;
    }

    private class PathMap extends HashMap<String, Set<String>> {

        public void add(String path, String id) {
            Set<String> ids = super.get(path);
            if (ids == null) {
                ids = new HashSet<String>();
                this.put(path, ids);
            }
            ids.add(id);
        }

        public void remove(String path, String id) {
            Set<String> ids = super.get(path);
            if (ids != null) {
                ids.remove(id);
            }
        }

    }

}
