package org.projectodd.restafari.spi.state;

/** Opaque state of a resource.
 *
 * <p>State objects are used to instill new state into
 * a server-side resource.</p>
 *
 * @author Bob McWhirter
 */
public interface ResourceState {

    /** Retrieve the ID of the resource.
     *
     * @return The ID of the resource.
     */
    String id();

    /** Set the ID of the resource.
     *
     * @param id The ID of the resource.
     */
    void id(String id);

}
