package io.liveoak.spi.resource;

import java.util.Collection;

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

    default Collection<? extends Resource> members(RequestContext ctx) throws Exception {
        return null;
    }

    default Resource member(RequestContext ctx, String id) throws Exception {
        return null;
    }

    default ResourceState properties() throws Exception {
        return null;
    }

    default void properties(ResourceState props) throws Exception {
        // nothing
    }

    @Override
    default void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            Collection<? extends Resource> members = members(ctx);
            if (members != null) {
                for (Resource each : members) {
                    sink.accept(each);
                }
            }
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }

    @Override
    default void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Resource member = member(ctx, id);
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
