package io.liveoak.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Bob McWhirter
 */
public class LiveOakJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.err.println( "firing job" );
    }

}
