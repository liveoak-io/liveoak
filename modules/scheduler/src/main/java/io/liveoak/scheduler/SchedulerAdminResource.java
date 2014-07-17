package io.liveoak.scheduler;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.quartz.Scheduler;

/**
 * @author Bob McWhirter
 */
public class SchedulerAdminResource implements RootResource, SynchronousResource {

    public SchedulerAdminResource(String id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    private Resource parent;
    private final String id;
    private Scheduler scheduler;

}
