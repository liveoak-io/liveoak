package org.projectodd.restafari.spi;

/** Responder for resource-deleation.
 * 
 * @author Bob McWhirter
 */
public interface DeleteResourceResponder {
    
    /** Respond with the deleted resource.
     * 
     * @param resource The deleted resource.
     */
    void resourceDeleted(Resource resource);

}
