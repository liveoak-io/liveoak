package io.liveoak.scheduler;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.quartz.JobExecutionContext;

import java.util.Date;

/**
 * @author Bob McWhirter
 */
public class FireResource implements Resource {

    public FireResource(TriggerResource parent, JobExecutionContext context) {
        this.parent = parent;
        this.id = context.getFireInstanceId();
        this.fireTime = context.getFireTime();
        this.scheduledFireTime = context.getScheduledFireTime();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        sink.accept( "fire_time", this.fireTime );
        sink.accept( "scheduled_fire_time", this.scheduledFireTime );
        sink.close();
    }

    private TriggerResource parent;
    private String id;

    private final Date fireTime;
    private final Date scheduledFireTime;
}
