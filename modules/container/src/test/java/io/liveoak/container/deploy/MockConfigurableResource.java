package io.liveoak.container.deploy;

import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Bob McWhirter
 */
@Configurable
public class MockConfigurableResource implements RootResource {

    @ConfigProperty
    private String name;

    @ConfigProperty
    private Integer age;

    public MockConfigurableResource(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }

    private String id;
}
