package org.projectodd.restafari.spi;

/** Responder for resource-deleation.
 * 
 * @author Bob McWhirter
 */
public interface DeleteResourceResponder<T extends Resource> {
    
    /** Respond with the deleted resource.
     * 
     * @param resource The deleted resource.
     */
    void resourceDeleted(T resource);

}
