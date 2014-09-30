package io.liveoak.scripts.resourcetriggered.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.liveoak.spi.ResourceAlreadyExistsException;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;

/**
 * Changes in registered scripts don't need to be super fast,
 * but they need to be reliable.
 *
 * This class provides proper synchronization.
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ScriptRegistry {

    private ScriptMap scripts;

    public ScriptRegistry() {
        this.scripts = new ScriptMap();
    }


    public synchronized void add(ResourceTriggeredScript script) {
        scripts.add(script);
    }

    public synchronized Set<ResourceTriggeredScript> getByTarget(String target) {
        return scripts.getByTarget(target);
    }

    public synchronized int size() {
        return scripts.size();
    }

    /**
     * Extract script id, and script definition from ResourceState, and add it to registry
     *
     * @param parent Parent resource
     * @param state Resource state containing script definition
     *
     * @return ResourceScript resource representing the added script
     * @throws io.liveoak.spi.ResourceAlreadyExistsException
     * @throws io.liveoak.spi.PropertyException
     */
    public synchronized ResourceScript addFromState(ResourceScripts parent, ResourceState state) throws Exception {
        if (scripts.get(state.id()) != null) {
            throw new ResourceAlreadyExistsException(state.id());
        }

        ResourceScript resourceScript = new ResourceScript(parent, state);
        scripts.add(resourceScript.getScript());

        return resourceScript;
    }

    public synchronized void updateScript(ResourceScript script) {
        if (scripts.get(script.id()) != null) {
            scripts.remove(script.id());
            scripts.add(script.getScript());
        }
    }

    public synchronized Collection<ResourceTriggeredScript> values() {
        return Collections.unmodifiableCollection(new ArrayList<>(scripts.values()));
    }

    public synchronized ResourceTriggeredScript get(String id) {
        return scripts.get(id);
    }

    public synchronized void remove(String id) {
        scripts.remove(id);
    }

    public synchronized Set<ResourceTriggeredScript> getByTarget(String target, ResourceTriggeredScript.FUNCTIONS function, Boolean enabled) {
        return scripts.getByTarget(target, function, enabled);
    }

    public synchronized Set<ResourceTriggeredScript> getByPath(String path, ResourceTriggeredScript.FUNCTIONS function, boolean enabled) {
        return scripts.getByPath(path, function, enabled);
    }

    /**
     * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
     */
    private class ScriptMap {

        private Map<String, ResourceTriggeredScript> idMap = new HashMap<String, ResourceTriggeredScript>();
        private PathMap pathMap = new PathMap();

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
}
