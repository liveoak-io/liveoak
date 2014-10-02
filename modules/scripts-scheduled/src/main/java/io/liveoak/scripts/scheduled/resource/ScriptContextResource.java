package io.liveoak.scripts.scheduled.resource;

import java.util.Date;

import io.liveoak.scripts.scheduled.manager.ScheduledScriptManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptContextResource implements Resource {

    public final static String ID = "context";

    ScheduledScriptResource parent;
    Scheduler scheduler;

    private static String TRIGGER_STATE = "trigger-state";
    private static String EXECUTIONS = "executions";
    private static String START_TIME = "start-time";
    private static String END_TIME = "end-time";
    private static String FINAL_FIRE_TIME = "final-fire-time";
    private static String PREVIOUS_FIRE = "previous-fire-time";
    private static String NEXT_FIRE = "next-fire-time";

    public ScriptContextResource(ScheduledScriptResource parent, Scheduler scheduler) {
        this.parent = parent;
        this.scheduler = scheduler;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        if (scheduler != null) {
            String resourceId = parent.id();

            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(resourceId));
            if (jobDetail != null && jobDetail.getJobDataMap() != null) {
                sink.accept(EXECUTIONS, jobDetail.getJobDataMap().get(ScheduledScriptManager.EXECUTIONS));
            } else {
                sink.accept(EXECUTIONS, 0);
            }

            Trigger trigger = scheduler.getTrigger(new TriggerKey(resourceId));

            Date startTime = trigger.getStartTime();
            if (startTime != null) {
                sink.accept(START_TIME, startTime.getTime());
            } else {
                sink.accept(START_TIME, null);
            }

            Date endTime = trigger.getEndTime();
            if (endTime != null) {
                sink.accept(END_TIME, endTime.getTime());
            } else {
                sink.accept(END_TIME, null);
            }

            Date finalEndTime = trigger.getFinalFireTime();
            if (finalEndTime != null) {
                sink.accept(FINAL_FIRE_TIME, finalEndTime.getTime());
            } else {
                sink.accept(FINAL_FIRE_TIME, null);
            }

            Date previousFire = trigger.getPreviousFireTime();
            if (previousFire != null) {
                sink.accept(PREVIOUS_FIRE, previousFire.getTime());
            }   else {
                sink.accept(PREVIOUS_FIRE, null);
            }

            Date nextFire = trigger.getNextFireTime();
            if (nextFire != null) {
                sink.accept(NEXT_FIRE, nextFire.getTime());
            } else {
                sink.accept(NEXT_FIRE, null);
            }

            Trigger.TriggerState triggerState =  scheduler.getTriggerState(new TriggerKey(resourceId));
            if (triggerState != null) {
                sink.accept(TRIGGER_STATE, triggerState.toString().toLowerCase());
            } else {
                sink.accept(TRIGGER_STATE, null);
            }
        }
        sink.close();
    }

}
