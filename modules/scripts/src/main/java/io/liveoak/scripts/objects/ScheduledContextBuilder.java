package io.liveoak.scripts.objects;

import org.quartz.JobExecutionContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledContextBuilder {

    String id;
    JobExecutionContext context;

    public ScheduledContextBuilder(String id, JobExecutionContext context) {
        this.id = id;
        this.context = context;
    }

    public ScheduledContext build() {
        return new ScheduledContext(id, context);
    }
}
