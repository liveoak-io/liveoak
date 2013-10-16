package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.state.ResourceState;

/** A resource which collects a set of other resources.
 *
 * @author Bob McWhirter
 */
public interface CollectionResource extends Resource {

    /** Read the state of this resource, optionally with pagination information.
     *
     * @param pagination Pagination information, which may indicate no pagination.
     * @param responder To respond to the action.
     */
    void read(Pagination pagination, Responder responder);

    /** Create a new child resource of this collection.
     *
     * @param state The state for the child, which may include an ID.
     * @param responder To respond to the action.
     */
    void create(ResourceState state, Responder responder);

    /** Write the members of this object to the provided sink.
     *
     * @param sink The sink to stream members to.
     */
    void writeMembers(ResourceSink sink);

}
