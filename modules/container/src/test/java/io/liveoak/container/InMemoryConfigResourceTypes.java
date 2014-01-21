package io.liveoak.container;

import java.io.File;
import java.net.URI;
import java.net.URL;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class InMemoryConfigResourceTypes implements RootResource {

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

    @ConfigProperty
    private File file;

    @ConfigProperty
    private Boolean flag;

    @ConfigProperty
    private URL url;

    @ConfigProperty
    private URI uri;

    @ConfigProperty
    private Double dbl;

    @ConfigProperty
    private Integer integer;



}
