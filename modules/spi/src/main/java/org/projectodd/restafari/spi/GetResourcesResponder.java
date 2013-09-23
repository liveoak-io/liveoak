package org.projectodd.restafari.spi;

import java.util.Collection;

public interface GetResourcesResponder extends BaseResponder {
    
    void respondWithResources(Collection<Resource> resources);
    AsyncResourcesResponder respondWithResourcesAsync();

}
