package io.liveoak.ups.system;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.ups.util.UPSHostCheck;

/**
 * @author Ken Finnigan
 */
public class PingResource implements Resource {

    public PingResource(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "ping";
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        String url = ctx.resourceParams().value("url");

        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("'url' parameter was not set, or was empty.");
        }

        sink.accept("valid", UPSHostCheck.isValid(url));
        sink.complete();
    }

    private Resource parent;
}
