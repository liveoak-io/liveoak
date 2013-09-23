package org.projectodd.restafari.spi;

public interface CreateResourceResponder extends BaseResponder {
    
    void resourceCreated(Resource resource);

}
