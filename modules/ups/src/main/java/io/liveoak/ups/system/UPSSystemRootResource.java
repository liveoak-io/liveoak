package io.liveoak.ups.system;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.ResourceSink;

/**
 * @author Ken Finnigan
 */
public class UPSSystemRootResource extends DefaultRootResource {
    public UPSSystemRootResource(String id) {
        super(id);
        pingResource = new PingResource(this);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(pingResource);
        sink.complete();
    }

    private PingResource pingResource;
}
