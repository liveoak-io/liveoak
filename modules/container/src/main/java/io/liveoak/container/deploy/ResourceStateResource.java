package io.liveoak.container.deploy;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceStateResource implements Resource {

    public ResourceStateResource(ResourceState state) {
        this.state = state;
    }

    public ResourceStateResource(Resource parent, ResourceState state) {
        this.parent = parent;
        this.state = state;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        for ( String name : state.getPropertyNames() ) {
            Object value = state.getProperty( name );
            if ( value instanceof ResourceState ) {
                value = new ResourceStateResource((ResourceState) value);
            }
            sink.accept(name, value );
        }

        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for ( ResourceState member : this.state.members() ) {
            sink.accept( new ResourceStateResource( this, member ) );
        }

        sink.close();
    }

    private Resource parent;
    private ResourceState state;
}
