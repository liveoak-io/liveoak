package io.liveoak.spi.resource;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface ConfigResource extends Resource {

    @Override
    default String id() {
        return ";config";
    }

}
