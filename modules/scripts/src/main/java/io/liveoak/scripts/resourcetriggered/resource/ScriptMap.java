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

    Map<String, Script> idMap = new HashMap<String, Script>();
    PathMap pathMap = new PathMap();

    public void add(Script script) {
        String id = script.getId();
        idMap.put(id, script);
        pathMap.add(script.getTarget(), id);
    }

    public void remove(String id) {
        Script script = idMap.get(id);
        if (script != null) {
            idMap.remove(id);
            pathMap.remove(script.getTarget(), id);
        }
    }

    public int size() {
        return idMap.size();
    }

    public Collection<Script> values() {
        return idMap.values();
    }

    public Script get(String id) {
        return idMap.get(id);
    }

    public Set<Script> getByPath(String path) {

        Set<Script> scripts = new TreeSet<>(new Script.PriorityComparator());

        Set<String> ids = pathMap.get(path);
        if (ids != null) {
            for (String id: ids) {
                scripts.add(idMap.get(id));
            }
        }

        return scripts;
    }

    public Set<Script> getByPath(String path, Script.FUNCTIONS function, Boolean enabled ) {
        Set<Script> scripts = new TreeSet<>(new Script.PriorityComparator());

        Set<String> ids = pathMap.get(path);

        if (ids != null) {
            for (String id: ids) {
                Script script = idMap.get(id);
                if (script.isEnabled() && script.getProvides().contains(function)) {
                    scripts.add(idMap.get(id));
                }
            }
        }

        return scripts;
    }

    public Set<Script> getByTarget(String target) {
        Set<Script> scripts = new TreeSet<>(new Script.PriorityComparator());
        for (String path: generatePaths(target)) {
            scripts.addAll(getByPath(path));
        }
        return scripts;
    }

    public Set<Script> getByTarget(String target, Script.FUNCTIONS function, Boolean enabled) {
        Set<Script> scripts = new TreeSet<>(new Script.PriorityComparator());
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
