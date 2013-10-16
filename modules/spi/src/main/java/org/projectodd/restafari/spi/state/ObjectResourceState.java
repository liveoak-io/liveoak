package org.projectodd.restafari.spi.state;

import java.util.stream.Stream;

/** State for an object resource.
 *
 * @author Bob McWhirter
 */
public interface ObjectResourceState extends ResourceState {

    /** Add a property to the state.
     *
     * <p>Property values may be either simple scalar
     * values, or complex {@link ResourceState} objects</p>
     *
     * @param name The name of the property.
     * @param value The value of the property.
     */
    void addProperty(String name, Object value);

    /** Retrieve a property value.
     *
     * @param name The property name.
     * @return The value of the property, as either a simple scalar, or as a
     * more complex {@link ResourceState}.
     */
    Object getProperty(String name);

    /** Retrieve the property members of this state.
     *
     * @return A stream to access the members.
     */
    Stream<? extends PropertyResourceState> members();

}
