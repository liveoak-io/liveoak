package io.liveoak.scripts.scheduled.manager;

import io.liveoak.scripts.common.ScriptManager;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.scripts.objects.ScheduledContext;
import io.liveoak.scripts.objects.ScheduledContextBuilder;
import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.scripts.scheduled.ScheduledScript;
import org.quartz.JobExecutionContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptManager extends ScriptManager {

    public static final String EXECUTIONS = "executions";

    public ScheduledScriptManager(ScriptConfig scriptConfig, LibraryManager libraryManager) {
        super(scriptConfig, libraryManager);
    }

    public void execute(ScheduledScript script, JobExecutionContext context) throws Exception {

        Long executions = (Long)context.getJobDetail().getJobDataMap().get(EXECUTIONS);
        if (executions == null) {
            executions = 0L;
        } else {
            executions = executions + 1L;
        }
        context.getJobDetail().getJobDataMap().put(EXECUTIONS, executions);

        ScheduledContext scheduleContext = new ScheduledContextBuilder(script.getId(), context).build();

        Object library = getLibrary(script);

        if (context.getPreviousFireTime() == null && script.getProvides().contains(ScheduledScript.FUNCTIONS.ONSTART)) {
            runScript(ScheduledScript.FUNCTIONS.ONSTART.getFunctionName(), script, scheduleContext, library);
        }

        if (script.getProvides().contains(ScheduledScript.FUNCTIONS.EXECUTE)) {
            runScript(ScheduledScript.FUNCTIONS.EXECUTE.getFunctionName(), script, scheduleContext, library);
        }

        if (context.getNextFireTime() == null && script.getProvides().contains(ScheduledScript.FUNCTIONS.ONEND)) {
            runScript(ScheduledScript.FUNCTIONS.ONEND.getFunctionName(), script, scheduleContext, library);
        }


    }
}
