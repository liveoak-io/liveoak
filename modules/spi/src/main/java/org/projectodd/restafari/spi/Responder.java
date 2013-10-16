package org.projectodd.restafari.spi;

import java.util.Collection;

public interface Responder {
    
    void resource(Resource resource);
    //TODO: Add something to the SPI so the container can handle pagination, indicating if there are more results, etc.
    void resources(Collection<Resource> resources);
    
    void resourceCreated(Resource resource);
    void resourceUpdated(Resource resource);
    void resourceDeleted(Resource resource);
    
    void noSuchCollection(String name);
    void noSuchResource(String id);
    void internalError(String message);
    void collectionDeleted(String name);
    
}
