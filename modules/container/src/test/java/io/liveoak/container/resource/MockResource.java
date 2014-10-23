package io.liveoak.container.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MockResource implements RootResource, SynchronousResource {

    String id;
    Resource parent;

    public MockResource(String id) {
        this.id = id;
    }

    public MockResource(String id, Resource parent) {
        this.id = id;
        this.parent = parent;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("application.name", ctx.application().name());
        sink.accept("application.id", ctx.application().id());

        sink.complete();
    }
}
