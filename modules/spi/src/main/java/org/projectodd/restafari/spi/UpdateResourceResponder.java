package org.projectodd.restafari.spi;

public interface UpdateResourceResponder extends CreateResourceResponder {
    
    void resourceUpdated(Resource resource);

}
