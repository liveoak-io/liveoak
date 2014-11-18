package io.liveoak.container.resource.mapping;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.mapper.MappingResource;
import io.liveoak.spi.resource.mapper.Property;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Ken Finnigan
 */
public class InMemoryConfigResource implements RootResource, MappingResource {

    private Resource parent;
    String id;

    public InMemoryConfigResource(String id) {
        this.id = id;
    }

    @Property
    private String path1;

    @Property(value = "other")
    private String path2;

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public String id() {
        return id;
    }

}
