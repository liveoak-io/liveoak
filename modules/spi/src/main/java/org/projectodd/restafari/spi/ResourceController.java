package org.projectodd.restafari.spi;


/** Primary interface for RESTful resource controller.
 * 
 * <p>
 * A single controller may ultimately be responsible for many collections
 * of resource, if the resources are handled the same.  For instance,
 * a single MongoDB controller may be used to manipulate resources in
 * a "Person" collection, a "Pet" collection and a "Sandwich" collection,
 * since the method of fetching and updating data within MongoDB is the 
 * same for any given collection.
 * </p>
 * 
 * @see ObjectResource
 * @see BinaryResource
 * 
 * @author Bob McWhirter
 */
public interface ResourceController {
    
    /** Initialize this instance of a controller.
     * 
     * <p>
     * Each controller may involve multiple instances, each running
     * within its own Vertx event-loop.  Use the initialize method
     * to setup any resources required during its lifecycle.
     * </p>
     * 
     * @param context The controller context.
     */
    void initialize(ControllerContext context) throws InitializationException;
    
    /** Destroy this instance of a controller.
     * 
     * <p>
     * Free any resources used by this controller.
     * </p>
     */
    void destroy();
    
    /** Retrieve a single resource from a collection.
     * 
     * @param context The request context.
     * @param collectionName The name of the collection.
     * @param id The identifier of the resource.
     * @param responder The responder to provide the response.
     */
    void getResource(RequestContext context, String collectionName, String id, Responder responder);
    
    /** Retreive all resources from a collection.
     * 
     * @param context The request context.
     * @param collectionName The name of the collection.
     * @param pagination The pagination details.
     * @param responder The responder to provide the response.
     */
    void getResources(RequestContext context, String collectionName, Pagination pagination, Responder responder);
    
    /** Create a new resource in a collection.
     * 
     * @param context The request context.
     * @param collectionName The name of the collection.
     * @param resource The new resource state.
     * @param responder The responded to provide the response.
     */
    void createResource(RequestContext context, String collectionName, Resource resource, Responder responder);
    
    /** Update (or create) a new resource in a collection.
     * 
     * <p>
     * The semantics of "update" is actually "upsert".  If a resource with the
     * specified ID does not exist, it should be "inserted", where if one does
     * exist, it should be updated in-place.
     * 
     * @param context The request context.
     * @param collectionName The name of the collection.
     * @param id The identifier of the resource.
     * @param resource The new resource state.
     * @param responder The responder to provide the response.
     */
    void updateResource(RequestContext context, String collectionName, String id, Resource resource, Responder responder);
    
    /** Detele a resource in a collection.
     * 
     * @param context The request context.
     * @param collectionName The name of the resource.
     * @param id The identifier of the resource.
     * @param responder The responder to provide the response.
     */
    void deleteResource(RequestContext context, String collectionName, String id, Responder responder);

}
