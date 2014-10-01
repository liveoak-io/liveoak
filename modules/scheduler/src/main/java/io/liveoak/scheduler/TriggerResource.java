package io.liveoak.scheduler;

import java.util.LinkedList;

import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.logging.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

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
            log.error("", e);
        }
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            this.fires.stream().forEach((e) -> {
                sink.accept(e);
            });
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }

    public void createFire(JobExecutionContext context) {
        FireResource fireResource = new FireResource(this, context);
        this.fires.add(fireResource);

        //TODO: maybe using ResourceResponse isn't the best solution here....
        ResourceResponse response = new DefaultResourceResponse(null, ResourceResponse.ResponseType.CREATED, fireResource);
        //response.setState();
        this.parent.notifier().resourceCreated(response);
    }


    private SchedulerResource parent;
    private Trigger trigger;
    private LinkedList<FireResource> fires = new LinkedList<>();

    private static final Logger log = Logger.getLogger(TriggerResource.class);
}
