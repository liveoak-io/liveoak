package io.liveoak.mongo.config;

import java.net.URI;

import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class EmbeddedConfigResource implements SynchronousResource {

    protected Resource parent;

    public EmbeddedConfigResource(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    //Embedded 'resources' have empty ids and uris
    @Override
    public String id() {
        return null;
    }

    @Override
    public URI uri() {
        return null;
    }
}
