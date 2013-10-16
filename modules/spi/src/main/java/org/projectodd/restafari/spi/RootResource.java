package org.projectodd.restafari.spi;

/** A root resource capable of being registered with a container.
 *
 * @author Bob McWhirter
 */
public interface RootResource extends Resource {

    /** Initialize the resource.
     *
     * @param context The initialization context.
     * @throws InitializationException If an error occurs.
     */
    void initialize(ResourceContext context) throws InitializationException;

    /** Free resources used by the resource and shutdown.
     */
    void destroy();
}
