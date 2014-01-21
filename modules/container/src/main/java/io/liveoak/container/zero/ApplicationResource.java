package io.liveoak.container.zero;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class ApplicationResource implements RootResource, SynchronousResource {

    public ApplicationResource(InternalApplication app) {
        this.app = app;
        this.extensions = new ApplicationExtensionsResource( this, "resources" );
    }

    @Override
    public String id() {
        return this.app.id();
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public InternalApplication application() {
        return this.app;
    }

    @Override
    public Collection<Resource> members() {
        return Collections.singletonList( this.extensions);
    }

    @Override
    public Resource member(String id) {
        if ( id.equals(this.extensions.id()) ) {
            return this.extensions;
        }
        return null;
    }

    public ApplicationExtensionsResource extensionsResource() {
        return this.extensions;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.app.organization().destroyApplication( this.app );
        responder.resourceDeleted( this );
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept( "id", this.app.id() );
        sink.accept( "name", this.app.name() );
        sink.accept( "directory", this.app.directory().getAbsolutePath() );
        sink.close();
    }

    private Resource parent;
    private InternalApplication app;
    private final ApplicationExtensionsResource extensions;


}
