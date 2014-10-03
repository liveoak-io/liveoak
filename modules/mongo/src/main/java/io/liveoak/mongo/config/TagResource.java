package io.liveoak.mongo.config;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

import java.net.URI;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class TagResource implements Resource {

    Resource parent;
    String tagName;
    Object value;

    public TagResource(Resource parent, String tagName, Object value) {
        this.parent = parent;
        this.tagName = tagName;
        this.value = value;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(tagName, value);
        sink.complete();
    }
}
