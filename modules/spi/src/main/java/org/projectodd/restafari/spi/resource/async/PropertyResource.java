package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.resource.Resource;

/** A simple key/value pair resource.
 *
 * <p>Typically used as members of an ObjectResource.</p>
 *
 * @see ObjectResource
 *
 * @author Bob McWhirter
 */
public interface PropertyResource extends Resource {

    /** Set the value.
     *
     * @param value The value.
     */
    void set(Object value);

    /** Get the value.
     *
     * @return The value.
     */
    Object get();
}
