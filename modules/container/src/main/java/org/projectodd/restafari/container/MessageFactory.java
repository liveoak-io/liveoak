package org.projectodd.restafari.container;

import java.util.Collection;

import org.projectodd.restafari.spi.Message;
import org.projectodd.restafari.spi.Resource;

public interface MessageFactory {
    
    Message resource(Resource resource);
    Message resources(Collection<Resource> resources);
    
    Message resourceCreated(Resource resource);
    Message resourceUpdated(Resource resource);
    Message resourceDeleted(Resource resource);
    
    Message noSuchCollection(String name);
    Message noSuchResource(String name);
    Message internalError(String name);

}
