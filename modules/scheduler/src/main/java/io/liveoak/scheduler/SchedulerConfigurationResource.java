package io.liveoak.scheduler;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class SchedulerConfigurationResource implements Resource {

    public SchedulerConfigurationResource(SchedulerResource scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Resource parent() {
        return this.scheduler;
    }

    @Override
    public String id() {
        return ";config";
    }

    private SchedulerResource scheduler;
}
