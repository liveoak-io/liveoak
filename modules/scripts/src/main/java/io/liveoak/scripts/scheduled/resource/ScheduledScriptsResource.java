package io.liveoak.scripts.scheduled.resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.scripts.resource.ScriptResource;
import io.liveoak.scripts.resource.ScriptsResource;
import io.liveoak.scripts.scheduled.ScheduledScript;
import io.liveoak.scripts.scheduled.manager.ScheduleManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

/**
 * Scripts to run based on an schedule.
 *
 * This is to script methods such as: at, until, cron/interval
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptsResource extends ScriptsResource {

    private static final String ID = "scheduled-scripts";

    private static final String RESOURCE_DIRNAME = "scheduled";
    private final ScheduleManager scheduleManager;
    private File scriptDirectory;

    private Map<String, ScheduledScript> scripts = new HashMap<>();

    public ScheduledScriptsResource(Vertx vertx, ScheduleManager scheduleManager) {
        super(vertx);
        this.scheduleManager = scheduleManager;
    }

    public ScheduleManager getScheduleManager() {
        return this.scheduleManager;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "Scheduled Scripts");
        sink.accept("description", "Scripts to be run based on a schedule.");
        sink.accept("count", scripts.size());

        if (scheduleManager != null && scheduleManager.getScheduler() != null) {
            sink.accept("scheduler", new SchedulerStateResource(this, scheduleManager.getScheduler()));
        }

        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (ScheduledScript script: scripts.values()) {
            sink.accept(new ScheduledScriptResource(this, script));
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        ScheduledScript script = scripts.get(id);
        if (script != null) {
            responder.resourceRead(new ScheduledScriptResource(this, script));
        } else {
            responder.noSuchResource(id);
        }
    }

    protected File getScriptsDirectory() {
        if (scriptDirectory == null) {
            //get a reference to where the scripts should be held
            String resourceBasedDir = parent.getScriptConfig().getScriptDirectory() + File.separator + "/" + RESOURCE_DIRNAME;
            scriptDirectory = new File(resourceBasedDir);

            // create the directory if it doesn't already exist
            if (!scriptDirectory.exists()) {
                scriptDirectory.mkdirs();
            }
        }
        return scriptDirectory;
    }

    @Override
    protected ScriptResource generateChildResource(ResourceState state) throws Exception {
        ScheduledScriptResource resource = new ScheduledScriptResource(this, state);
        scripts.put(resource.id(), resource.getScript());
        return resource;
    }

}
