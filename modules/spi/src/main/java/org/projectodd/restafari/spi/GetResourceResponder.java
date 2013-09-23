package org.projectodd.restafari.spi;

public interface GetResourceResponder extends BaseResponder {
    
    void respondWithResource(Resource resource);

}
