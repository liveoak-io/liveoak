package io.liveoak.scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

/**
 * @author Bob McWhirter
 */
public class TriggerResource implements SynchronousResource {

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("cron", ((CronTrigger) this.trigger).getCronExpression());

        Trigger.TriggerState state = this.parent.scheduler().getTriggerState(this.trigger.getKey());
        result.put("state", state.toString().toLowerCase());
        return result;
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) throws Exception {
        return this.fires;
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
