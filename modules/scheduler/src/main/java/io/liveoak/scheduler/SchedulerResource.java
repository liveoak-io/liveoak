package io.liveoak.scheduler;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.ConfigurableResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class SchedulerResource implements RootResource, ConfigurableResource {

    public SchedulerResource(String id) {
        this.id = id;
        this.configResource = new SchedulerConfigurationResource(this);
    }

    @Override
    public String id() {
        return this.id;
    }

    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            this.scheduler = factory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new InitializationException(e);
        }

        this.notifier = context.notifier();
    }

    @Override
    public void destroy() {
        try {
            this.scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        if (this.scheduler.isStarted()) {
            sink.accept("status", "started");
        } else {
            sink.accept("stats", "stopped");
        }

        sink.accept("name", this.scheduler.getSchedulerName());
        sink.accept("instance-id", this.scheduler.getSchedulerInstanceId());
        SchedulerMetaData metaData = this.scheduler.getMetaData();

        sink.accept("running-since", metaData.getRunningSince());

        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        TriggerResource resource = this.children.get(id);
        if (resource == null) {
            responder.noSuchResource(id);
        } else {
            responder.resourceRead(resource);
        }
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

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.children.values().stream().forEach((e) -> {
            sink.accept(e);
        });
        sink.close();
    }

    @Override
    public Resource configuration() {
        return this.configResource;
    }

    Notifier notifier() {
        return this.notifier;
    }

    private String id;
    private Scheduler scheduler;
    private Map<String, TriggerResource> children = new HashMap<>();
    private Notifier notifier;
    private final SchedulerConfigurationResource configResource;

}
