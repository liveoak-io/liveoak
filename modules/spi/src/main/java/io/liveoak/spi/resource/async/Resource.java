package io.liveoak.spi.resource.async;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic server-side representation of a resource.
 *
 * <p>The basic function of a resource is that it may (optionally) contain
 * children, in the form of members-of-a-collection, or properties-of-an-object.</p>
 *
 * <p>Additionally, it may optionally be deleted.</p>
 *
 * <p>All resources optionally have an ID, if they are to be directly addressable.</p>
 *
 * <p>Each resource, if it is a child of another resource, should include a non-null
 * reference to its parent</p>
 *
 * @author Bob McWhirter
 */
public interface Resource {

    /**
     * Retrieve the URI associated with this resource.
     *
     * @return The URI
     */
    default URI uri() {
        List<String> segments = new ArrayList<>();
        Resource current = this;

        while (current != null) {
            segments.add(0, current.id());
            current = current.parent();
        }

        StringBuilder buf = new StringBuilder();

        if ( segments.size() == 1 && segments.get(0).equals( "" ) ) {
            return URI.create( "/" );
        }

        segments.forEach((s) -> {
            if (s != null && !"".equals(s)) {
                buf.append("/");
                buf.append(s);
            }
        });

        return URI.create(buf.toString());
    }

    /**
     * Retrieve the parent resource of this resource, if any.
     *
     * @return The parent, or {@code null} if none.
     */
    Resource parent();

    /**
     * Retrieve the identifier of this resource, if any.
     *
     * @return The id, or {@code null} if none.
     */
    String id();

    /**
     * Read the properties of this resource.
     *
     * @return The read-only properties.
     */
    default void readProperties(RequestContext ctx, PropertySink sink) {
        sink.close();
    }

    /** Update this object's state.
     *
     * @param state The inbound representation of the state.
     * @param responder To respond to the action.
     */
    default void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {
        responder.updateNotSupported( this );
    }

    /** Create a new child resource of this collection.
     *
     * @param state The state for the child, which may include an ID.
     * @param responder To respond to the action.
     */
    default void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        responder.createNotSupported( this );
    }

    /** Write the members of this object to the provided sink.
     *
     * @param sink The sink to stream members to.
     */
    default void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.close();
    }

    default void readMember(RequestContext ctx, String id, Responder responder) {
        responder.noSuchResource( id );
    }

    /**
     * Delete this resource.
     *
     * @param responder To respond to the action.
     */
    default void delete(RequestContext ctx, Responder responder) {
        responder.deleteNotSupported( this );
    }
}
