package org.projectodd.restafari.spi;

/** Responder for resource creation.
 * 
 * @author Bob McWhirter
 */
public interface CreateResourceResponder<T extends Resource> extends BaseResponder {
    
    /** Respond with the created resource.
     * 
     * @param resource The created resource.
     */
    void resourceCreated(T resource);

}
