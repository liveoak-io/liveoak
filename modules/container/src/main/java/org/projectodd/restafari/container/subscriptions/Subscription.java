package org.projectodd.restafari.container.subscriptions;

import org.projectodd.restafari.spi.ResourcePath;
import org.projectodd.restafari.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface Subscription {

    String id();

    ResourcePath resourcePath();

    void resourceCreated(Resource resource) throws Exception;
    void resourceUpdated(Resource resource) throws Exception;
    void resourceDeleted(Resource resource) throws Exception;

}
