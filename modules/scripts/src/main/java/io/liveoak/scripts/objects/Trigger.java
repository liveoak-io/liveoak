package io.liveoak.scripts.objects;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Trigger {

    Scheduler scheduler;
    TriggerKey triggerKey;
    org.quartz.Trigger quartzTrigger;

    Trigger(String id, Scheduler scheduler) throws Exception {
        this.scheduler = scheduler;
        this.triggerKey = new TriggerKey(id);
        quartzTrigger = scheduler.getTrigger(triggerKey);
    }

    public String getState() throws Exception{
        TriggerState triggerState = scheduler.getTriggerState(triggerKey);
        if (triggerState != null) {
            return triggerState.toString().toLowerCase();
        } else {
            return null;
        }
    }

    public Date getStartTime() {
        Date startTime = quartzTrigger.getStartTime();
        if (startTime != null) {
            return new Date(startTime.getTime());
        } else {
            return null;
        }
    }

    public Date getEndTime() {
        Date endTime = quartzTrigger.getEndTime();
        if (endTime != null) {
            return new Date(endTime.getTime());
        } else {
            return null;
        }
    }

    public Date getFinalFireTime() {
        Date finalFireTime = quartzTrigger.getFinalFireTime();
        if (finalFireTime != null) {
            return new Date(finalFireTime.getTime());
        } else {
            return null;
        }
    }

    public Date getNextFireTime() {
        Date nextFireTime = quartzTrigger.getNextFireTime();
        if (nextFireTime != null) {
            return new Date(nextFireTime.getTime());
        } else {
            return null;
        }
    }

    public Date getPreviousFireTime() {
        Date previousFireTime = quartzTrigger.getPreviousFireTime();
        if (previousFireTime != null) {
            return new Date(previousFireTime.getTime());
        } else {
            return null;
        }
    }
}
