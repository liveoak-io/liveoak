package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

/** A resource which collects a set of other resources.
 *
 * @author Bob McWhirter
 */
public interface CollectionResource extends Resource {

    /** Create a new child resource of this collection.
     *
     * @param state The state for the child, which may include an ID.
     * @param responder To respond to the action.
     */
    void create(RequestContext ctx, ResourceState state, Responder responder);

    /** Write the members of this object to the provided sink.
     *
     * @param sink The sink to stream members to.
     */
    void readMembers(RequestContext ctx, ResourceSink sink);

}
