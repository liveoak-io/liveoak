package io.liveoak.container.extension;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class MockAdminResource extends MockResource {

    private final String flavor;
    private ResourceState props;

    public MockAdminResource(String id) {
        this(id, "default'");
    }

    public MockAdminResource(String id, String flavor) {
        super(id);
        this.flavor = flavor;
    }

    public String flavor() {
        return this.flavor;
    }

    @Override
    public ResourceState properties() throws Exception {
        return this.props;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        this.props = props;

    }
}
