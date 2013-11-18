package io.liveoak.scheduler;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.responders.BaseResponder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Bob McWhirter
 */
public class LiveOakJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        TriggerResource trigger = (TriggerResource) context.getMergedJobDataMap().get( "resource" );

        trigger.createFire( context );
    }
}
