package org.projectodd.restafari.spi.resource;

import org.projectodd.restafari.spi.resource.async.Responder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** Basic server-side representation of a resource.
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

    /** Retrieve the URI associated with this resource.
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

        segments.forEach((s) -> {
            buf.append( "/" );
            buf.append( s );
        });

        return URI.create( buf.toString() );
    }

    /** Retrieve the parent resource of this resource, if any.
     *
     * @return The parent, or {@code null} if none.
     */
    Resource parent();

    /** Retrieve the identifier of this resource, if any.
     *
     * @return The id, or {@code null} if none.
     */
    String id();

    /** Locate a child resource.
     *
     * <p>Depending on the modelling of this resource, the child
     * may be a member-of-a-collection, or it might be a property-of-an-object.</p>
     *
     * <p>Semantics are left to the author of the resource.</p>
     *
     * @param id The child ID to read.
     * @param responder To respond to the action.
     */
    void read(String id, Responder responder);

    /** Delete this resource.
     *
     * @param responder To respond to the action.
     */
    void delete(Responder responder);
}
