package io.liveoak.scripts.scheduled;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * Scripts to run based on an schedule.
 *
 * This is to script methods such as: at, until, cron/interval
 *
 * TODO: implement
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScripts implements Resource {

    private Resource parent;
    private static final String ID = "scheduled-scripts";

    public ScheduledScripts(Resource parent) {
        this.parent = parent;
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
        sink.accept("name", "Scheduled Scripts");
        sink.accept("description", "Scripts to be run based on a schedule.");
        sink.accept("count", 0);
        sink.close();
    }
}
