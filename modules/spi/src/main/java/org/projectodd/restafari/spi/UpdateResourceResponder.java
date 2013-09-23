package org.projectodd.restafari.spi;


/** Responder for resource updates.
 * 
 * <p>
 * Since updates may include inserts, this responder may also indicate 
 * resource creation.
 * </p>
 * 
 * @author Bob McWhirter
 */
public interface UpdateResourceResponder extends CreateResourceResponder {
    
    /** Respond with the updated resource.
     * 
     * @param resource The updated resource.
     */
    void resourceUpdated(Resource resource);

}
