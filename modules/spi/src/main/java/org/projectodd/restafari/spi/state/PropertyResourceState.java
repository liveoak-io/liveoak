package org.projectodd.restafari.spi.state;

/** A key/value state.
 *
 * @author Bob McWhirter
 */
public interface PropertyResourceState extends ResourceState {

    /** Retrieve the value.
     *
     * @return The value.
     */
    Object value();


}
