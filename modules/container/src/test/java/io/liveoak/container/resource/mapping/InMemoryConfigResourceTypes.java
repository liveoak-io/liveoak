package io.liveoak.container.resource.mapping;

import java.io.File;
import java.net.URI;
import java.net.URL;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.mapper.MappingResource;
import io.liveoak.spi.resource.mapper.Property;

/**
 * @author Ken Finnigan
 */
public class InMemoryConfigResourceTypes implements RootResource, MappingResource {

    String id;

    public InMemoryConfigResourceTypes(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    private Resource parent;

    @Property
    private File file;

    @Property
    private Boolean flag;

    @Property
    private URL url;

    @Property
    private URI uri;

    @Property
    private Double dbl;

    @Property
    private Integer integer;



}
