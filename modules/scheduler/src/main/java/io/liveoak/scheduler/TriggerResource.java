package io.liveoak.scheduler;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.net.URI;

/**
 * @author Bob McWhirter
 */
public class TriggerResource implements Resource {

    public TriggerResource(SchedulerResource parent, Trigger trigger) {
        this.parent = parent;
        this.trigger = trigger;
    }

    @Override
    public String id() {
        return this.trigger.getJobKey().getName();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        sink.accept( "cron", ((CronTrigger)this.trigger).getCronExpression() );
        try {
            Trigger.TriggerState state = this.parent.scheduler().getTriggerState(this.trigger.getKey());
            sink.accept( "state", state.toString().toLowerCase() );
        } catch (SchedulerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        sink.close();
    }

    private SchedulerResource parent;
    private Trigger trigger;

}
