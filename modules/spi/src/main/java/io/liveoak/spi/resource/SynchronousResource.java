package io.liveoak.spi.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface SynchronousResource extends Resource {

    default Collection<Resource> members() {
        return null;
    }

    default Resource member(String id) {
        return null;
    }

    default ResourceState properties() {
        return null;
    }

    default void properties(ResourceState props) {
        // nothing
    }

    @Override
    default void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        Collection<Resource> members = members();

        if (members != null) {
            for (Resource each : members) {
                sink.accept(each);
            }
        }

        sink.close();
    }

    @Override
    default void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Resource member = member(id);
        if (member == null) {
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(member);
    }

    @Override
    default void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        ResourceState props = properties();

        if (props != null) {
            for (String key : props.getPropertyNames() ) {
                sink.accept(key, props.getProperty( key ) );
            }
        }

        sink.close();
    }

    @Override
    default void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        properties( state );
        responder.resourceUpdated( this );
    }
}
