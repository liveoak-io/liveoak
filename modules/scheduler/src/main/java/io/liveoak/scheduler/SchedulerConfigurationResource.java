package io.liveoak.scheduler;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class SchedulerConfigurationResource implements ConfigResource {

    public SchedulerConfigurationResource(SchedulerResource scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Resource parent() {
        return this.scheduler;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.resourceUpdated( this );
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.close();
    }

    private SchedulerResource scheduler;
}
