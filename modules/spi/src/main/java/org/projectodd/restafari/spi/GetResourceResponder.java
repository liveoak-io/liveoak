package org.projectodd.restafari.spi;

/** Responder which expects exactly one resource.
 * 
 * @author Bob McWhirter
 */
public interface GetResourceResponder<T extends Resource> extends BaseResponder {
    
    /** Respond with a resource, ending the interaction.
     * 
     * @param resource The resource.
     */
    void respondWithResource(Resource resource);

}
