package org.projectodd.restafari.spi;

/** Responder for resource creation.
 * 
 * @author Bob McWhirter
 */
public interface CreateResourceResponder extends BaseResponder {
    
    /** Respond with the created resource.
     * 
     * @param resource The created resource.
     */
    void resourceCreated(Resource resource);

}
