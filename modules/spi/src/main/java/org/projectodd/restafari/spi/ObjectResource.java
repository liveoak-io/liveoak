package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.stream.Stream;

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
    void update(ObjectResourceState state, Responder responder);


    /** Write the members of this object to the provided sink.
     *
     * @param sink The sink to stream members to.
     */
    void writeMembers(ResourceSink sink);

}
