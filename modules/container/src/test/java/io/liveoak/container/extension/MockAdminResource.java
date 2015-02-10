package io.liveoak.container.extension;

import java.util.List;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        return new DefaultResourceState(this.props).propertyMap();
    }

    @Override
    public void properties(RequestContext ctx, ResourceState props) throws Exception {
        this.props = props;
        this.props.putProperty("unknownDir", "/my/unknown/path");

        for (String name : props.getPropertyNames()) {
            Object value = props.getProperty(name);
            if (value instanceof List) {
                this.props.putProperty(name, ResourceConversionUtils.convertList((List<ResourceState>)value, this));
            }
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        if (parent() != null) {
            parent().deleteMember(ctx, this.id(), responder);
        } else {
            responder.resourceDeleted(this);
        }
    }
}
