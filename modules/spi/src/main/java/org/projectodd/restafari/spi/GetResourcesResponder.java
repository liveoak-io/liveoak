package org.projectodd.restafari.spi;

import java.util.Collection;

/** Responder expecting a collection of resources.
 * 
 * <p>
 * If the enter collection is known at one point in time,
 * {@link #respondWithResources(Collection)} may be used once,
 * ending the interaction.
 * </p>
 * 
 * <p>
 * If the collection is to be provided in a piece-meal/streaming
 * fashion, a {@link AsyncResourcesResponder} may be retrieved,
 * using {@link #respondWithResourcesAsync()}.
 * </p>
 * 
 * @author Bob McWhirter
 */
public interface GetResourcesResponder extends BaseResponder {
    
    
    /** Respond with a collection of resources, ending the interaction.
     * 
     * @param resources The collection.
     */
    void respondWithResources(Collection<Resource> resources);
    
    /** Retrieve a streamable responder.
     * 
     * <p>
     * The interaction will end when {@link AsyncResourcesResponder#end()} is called.
     * </p>
     * 
     * @return
     */
    AsyncResourcesResponder respondWithResourcesAsync();

}
