package io.liveoak.scripts.scheduled.manager;

import java.util.Date;

import io.liveoak.scripts.scheduled.ScheduledScript;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduleManager {

    Scheduler scheduler;
    ScheduledScriptManager scriptManager;

    public static final String SCRIPT_DATAMAP_KEY = "scheduledScript";
    public static final String SCRIPT_MANAGER_DATAMAP_KEY = "scriptManager";

    public ScheduleManager(Scheduler scheduler,ScheduledScriptManager scriptManager ) {
        this.scheduler = scheduler;
        this.scriptManager = scriptManager;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void handleScript(ScheduledScript script) throws Exception {
        if (checkScript(script)) {
            addScript(script);
        } else {
            removeScript(script);
        }
    }

    private void addScript(ScheduledScript script) throws Exception {
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(script.getId());

        if (script.getCron() != null) {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(script.getCron()));
        }

        if (script.getAt() != null) {
            triggerBuilder.startAt(script.getAt());
        }

        if (script.getUntil() != null) {
            triggerBuilder.endAt(script.getUntil());
        }

        Trigger trigger = triggerBuilder.build();

        JobDataMap dataMap = new JobDataMap();
        dataMap.put(SCRIPT_DATAMAP_KEY, script);
        dataMap.put(SCRIPT_MANAGER_DATAMAP_KEY, scriptManager);

        JobBuilder jobBuilder = JobBuilder.newJob();
        jobBuilder.withIdentity(script.getId());
        jobBuilder.ofType(ScriptJob.class);
        jobBuilder.setJobData(dataMap);

        scheduler.scheduleJob(jobBuilder.build(), trigger);
    }

    private void removeScript(ScheduledScript script) throws Exception {
        JobKey key = new JobKey(script.getId());
        if (scheduler.checkExists(key)) {
            scheduler.deleteJob(new JobKey(script.getId()));
        }
    }

    private Boolean checkScript(ScheduledScript script) {
        // if the script isn't enabled, then don't add it
        if (!script.isEnabled()) {
            return false;
        }

        Long currentTime = new Date().getTime();

        // If the until time has already passed, then don't add it
        if (script.getUntil() != null && script.getUntil().getTime() < currentTime) {
            return false;
        }

        // If we have a at value, and not a cron value, then check if the at value has already passed or not
        if (script.getAt() != null && script.getCron() == null && script.getAt().getTime() < currentTime) {
            return false;
        }

        return true;
    }
}
