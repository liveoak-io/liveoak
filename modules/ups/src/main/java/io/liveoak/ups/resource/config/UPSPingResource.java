package io.liveoak.ups.resource.config;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.ups.util.UPSHostCheck;

/**
 * @author Ken Finnigan
 */
public class UPSPingResource implements Resource {
    public UPSPingResource(UPSRootConfigResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return "ping";
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("valid", UPSHostCheck.isValid(parent.getUPSServerURL()));
        sink.complete();
    }

    private UPSRootConfigResource parent;
}
