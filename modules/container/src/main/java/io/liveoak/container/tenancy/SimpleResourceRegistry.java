package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class SimpleResourceRegistry implements MountPointResource, RootResource, SynchronousResource {

    public SimpleResourceRegistry(Resource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public SimpleResourceRegistry(String id) {
        this.id = id;
    }

    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void registerResource(Resource resource) {
        this.registry.put(resource.id(), resource);
    }

    public void unregisterResource(Resource resource) {
        this.registry.remove(resource.id());
    }

    @Override
    public Collection<Resource> members() {
        return this.registry.values();
    }

    @Override
    public Resource member(String id) {
        return this.registry.get(id);
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": parent=" + this.parent + "; id=" + this.id + "; registry=" + this.registry.keySet() + "]";
    }

    private Resource parent;
    private String id;
    private Map<String, Resource> registry = new ConcurrentHashMap<>();
}
