package org.projectodd.restafari.spi;

public interface ResourceController {
    
    void initialize(ControllerContext context);
    
    void getResource(String collectionName, String id, GetResourceResponder responder);
    void getResources(String collectionName, Pagination pagination, GetResourcesResponder responder);
    
    void createResource(String collectionName, Resource resource, CreateResourceResponder responder);
    void updateResource(String collectionName, Resource resource, UpdateResourceResponder responder);
    void deleteResource(String collectionName, String id, DeleteResourceResponder responder);

}
