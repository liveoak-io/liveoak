package io.liveoak.container.tenancy;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface MountPointResource extends Resource {
    void registerResource(Resource resource);

    default void registerResource(Resource resource, MediaType mediaType, boolean makeDefault) {
        this.registerResource(resource);
    }

    void unregisterResource(Resource resource);

    default void unregisterResource(Resource resource, MediaType mediaType) {
        this.unregisterResource(resource);
    }
}
