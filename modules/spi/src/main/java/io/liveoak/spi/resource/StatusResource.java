package io.liveoak.spi.resource;

import java.net.URI;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class StatusResource implements Resource {

    private String id;
    private URI uri;
    private ResourceProcessingException error;

    public StatusResource(URI uri) {
        this.uri = uri;
    }

    public StatusResource(URI uri, ResourceProcessingException e) {
        this.uri = uri;
        this.error = e;
        id = new ResourcePath(uri.toString()).tail().toString();
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        if (error != null) {
            sink.accept("error-type", error.errorType());
            if (error.getMessage() != null) {
                sink.accept("message", error.getMessage());
            }
            if (error.getCause() != null) {
                sink.accept("cause", error.getCause().toString());
            }
        }
        sink.close();
    }
}
