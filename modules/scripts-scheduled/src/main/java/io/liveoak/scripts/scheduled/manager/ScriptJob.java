package io.liveoak.scripts.scheduled.manager;

import io.liveoak.scripts.scheduled.ScheduledScript;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
// The following annotations allow for us to store an execution count for the job
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ScriptJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ScheduledScript script = (ScheduledScript) context.getMergedJobDataMap().get(ScheduleManager.SCRIPT_DATAMAP_KEY);
        ScheduledScriptManager scriptManager = (ScheduledScriptManager) context.getMergedJobDataMap().get(ScheduleManager.SCRIPT_MANAGER_DATAMAP_KEY);
        try {
            scriptManager.execute(script, context);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
