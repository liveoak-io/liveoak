package io.liveoak.scheduler;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.LinkedList;

/**
 * @author Bob McWhirter
 */
public class TriggerResource implements Resource {

    public TriggerResource(SchedulerResource parent, Trigger trigger) {
        this.parent = parent;
        this.trigger = trigger;
    }

    @Override
    public String id() {
        return this.trigger.getJobKey().getName();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("cron", ((CronTrigger) this.trigger).getCronExpression());
        try {
            Trigger.TriggerState state = this.parent.scheduler().getTriggerState(this.trigger.getKey());
            sink.accept("state", state.toString().toLowerCase());
        } catch (SchedulerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.fires.stream().forEach((e) -> {
            sink.accept(e);
        });
        sink.close();
    }

    public void createFire(JobExecutionContext context) {
        FireResource fireResource = new FireResource(this, context);
        this.fires.add(fireResource);
        this.parent.notifier().resourceCreated(fireResource);
    }


    private SchedulerResource parent;
    private Trigger trigger;
    private LinkedList<FireResource> fires = new LinkedList<>();

}
