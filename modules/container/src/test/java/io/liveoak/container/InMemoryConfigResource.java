package io.liveoak.container;

import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.resource.RootResource;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class InMemoryConfigResource implements RootResource {

    String id;

    public InMemoryConfigResource(String id) {
        this.id = id;
    }

    @ConfigProperty
    private String path1;

    @ConfigProperty(value = "other")
    private String path2;

    @Override
    public String id() {
        return id;
    }
}
