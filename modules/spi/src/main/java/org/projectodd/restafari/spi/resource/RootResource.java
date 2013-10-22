package org.projectodd.restafari.spi.resource;

import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.ResourceContext;

/** A root resource capable of being registered with a container.
 *
 * @author Bob McWhirter
 */
public interface RootResource extends Resource {

    /** Initialize the resource.
     *
     * @param context The initialization context.
     * @throws org.projectodd.restafari.spi.InitializationException If an error occurs.
     */
    void initialize(ResourceContext context) throws InitializationException;

    /** Free resources used by the resource and shutdown.
     */
    void destroy();


    default Resource parent() {
        return null;
    }
}
