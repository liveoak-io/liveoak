package io.liveoak.scripts.objects;

import org.quartz.Scheduler;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class TriggerBuilder {

    Scheduler scheduler;
    String id;

    public TriggerBuilder(String id, Scheduler scheduler) {
        this.id = id;
        this.scheduler = scheduler;
    }

    public Trigger build() throws Exception {
        return new Trigger(id, scheduler);
    }
}
