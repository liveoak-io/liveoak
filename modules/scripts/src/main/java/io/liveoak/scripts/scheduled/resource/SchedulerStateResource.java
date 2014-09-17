package io.liveoak.scripts.scheduled.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.quartz.Scheduler;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SchedulerStateResource implements Resource {

    ScheduledScriptsResource parent;
    Scheduler scheduler;

    // The property names
    public static final String NAME = "name";
    public static final String INSTANCE_ID = "instance-id";
    public static final String STATUS = "status";
    public static final String RUNNING_SINCE = "running-since";


    // The different status values a scheduler state can be in
    private static final String STARTED = "started";
    private static final String STANDBY = "standby";
    private static final String STOPPED = "stopped";

    public SchedulerStateResource(ScheduledScriptsResource parent, Scheduler scheduler) {
        this.parent = parent;
        this.scheduler = scheduler;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(NAME, scheduler.getSchedulerName());
        sink.accept(INSTANCE_ID, scheduler.getSchedulerInstanceId());
        sink.accept(RUNNING_SINCE, scheduler.getMetaData().getRunningSince().getTime());

        if (scheduler.isStarted()) {
            sink.accept(STATUS, STARTED);
        } else if (scheduler.isInStandbyMode()) {
            sink.accept(STATUS, STANDBY);
        } else {
            sink.accept(STATUS, STOPPED);
        }

        sink.close();
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }
}
