package org.projectodd.restafari.spi;

/** Responder capable of sending a series of individual resources as a collection.
 * 
 * <p>
 * The response is considered open until {@link #end()} is called.
 * 
 * @author Bob McWhirter
 */
public interface AsyncResourcesResponder<T extends Resource> {
    
    /** Add a resource to the response.
     * 
     * @param resource The resource.
     */
    void respondWithResource(T resource);
    
    /** End the interaction.
     * 
     * <p>
     * The responder may not be used after this method is called.
     * </p>
     */
    void end();

}
