package io.liveoak.scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * @author Bob McWhirter
 */
public class SchedulerResource implements RootResource, SynchronousResource {

    public SchedulerResource(String id, Scheduler scheduler, Notifier notifier) {
        this.id = id;
        this.scheduler = scheduler;
        this.notifier = notifier;
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

    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (this.scheduler.isStarted()) {
            result.put("status", "started");
        } else {
            result.put("stats", "stopped");
        }

        result.put("name", this.scheduler.getSchedulerName());
        result.put("instance-id", this.scheduler.getSchedulerInstanceId());

        SchedulerMetaData metaData = this.scheduler.getMetaData();
        result.put("running-since", metaData.getRunningSince());
        return result;
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        TriggerResource resource = this.children.get(id);
        if (resource != null) {
            return resource;
        }
        return null;
    }

    @Override
    public Collection<? extends Resource> members(RequestContext ctx) throws Exception {
        return this.children.values();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        String id = UUID.randomUUID().toString();

        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule((String) state.getProperty("cron")));
        triggerBuilder.withIdentity(id);
        Trigger trigger = triggerBuilder.build();

        TriggerResource resource = new TriggerResource(this, trigger);

        JobDataMap dataMap = new JobDataMap();
        dataMap.put("resource", resource);

        JobBuilder jobBuilder = JobBuilder.newJob();
        jobBuilder.withIdentity(id);
        jobBuilder.ofType(LiveOakJob.class);
        jobBuilder.setJobData(dataMap);

        JobDetail jobDetail = jobBuilder.build();

        this.scheduler.scheduleJob(jobDetail, trigger);
        this.children.put(id, resource);
        responder.resourceCreated(resource);
    }

    Notifier notifier() {
        return this.notifier;
    }

    private Resource parent;
    private String id;
    private Scheduler scheduler;
    private Map<String, TriggerResource> children = new HashMap<>();
    private Notifier notifier;

    private static final Logger log = Logger.getLogger(SchedulerResource.class);

}
