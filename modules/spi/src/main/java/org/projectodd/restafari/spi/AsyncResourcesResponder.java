package org.projectodd.restafari.spi;

public interface AsyncResourcesResponder {
    
    void respondWithResource(Resource resource);
    void end();

}
