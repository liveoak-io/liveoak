package io.liveoak.container.zero;

import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class ResourceResource implements SynchronousResource, MountPointResource {

    public ResourceResource(ApplicationExtensionsResource parent, InternalApplicationExtension resource, String id) {
        this.parent = parent;
        this.resource = resource;
        this.id = id;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void registerResource(Resource resource) {
        this.config = resource;
    }

    @Override
    public void unregisterResource(Resource resource) {
        this.config = null;
    }

    @Override
    public Collection<? extends Resource> members() {
        if (this.config != null) {
            return Collections.singletonList(this.config);
        }
        return Collections.emptyList();
    }

    @Override
    public Resource member(String id) {
        if ( this.config == null ) {
            return null;
        }

        if ( this.config.id().equals( id ) ) {
            return this.config;
        }

        return null;
    }

    private ApplicationExtensionsResource parent;
    private InternalApplicationExtension resource;
    private String id;
    private Resource config;
}
