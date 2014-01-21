package io.liveoak.container.tenancy;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface MountPointResource extends Resource {
    void registerResource(Resource resource);
    void unregisterResource(Resource resource);
}
