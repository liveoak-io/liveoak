package org.projectodd.restafari.spi;

import java.util.Collection;

public interface Responder {
    
    void resource(Resource resource);
    void resources(Collection<Resource> resources);
    
    void resourceCreated(Resource resource);
    void resourceUpdated(Resource resource);
    void resourceDeleted(Resource resource);
    
    void noSuchCollection(String name);
    void noSuchResource(String id);
    void internalError(String message);
    
}
