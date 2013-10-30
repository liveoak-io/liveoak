package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

/** An object-like resource.
 *
 * <p>An object-like resource contains children that are indexed
 * by their names, ostensibly as properties.  The resource may support
 * updating of these properties.</p>
 *
 * @author Bob McWhirter
 */
public interface ObjectResource extends Resource {

    /** Update this object's state.
     *
     * @param state The inbound representation of the state.
     * @param responder To respond to the action.
     */
    void update(RequestContext ctx, ObjectResourceState state, Responder responder);


    /** Write the members of this object to the provided sink.
     *
     * @param sink The sink to stream members to.
     */
    void readContent(RequestContext ctx, ResourceSink sink);

}
