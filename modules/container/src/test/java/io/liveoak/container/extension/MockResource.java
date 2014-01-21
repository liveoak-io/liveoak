package io.liveoak.container.extension;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class MockResource implements RootResource {

    public MockResource(String id) {
        this.id = id;
    }

    @Override
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

    private String id;
    private Resource parent;
}
