package io.liveoak.scripts.scheduled.resource;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.scripts.resource.ScriptResource;
import io.liveoak.scripts.resource.ScriptsResource;
import io.liveoak.scripts.scheduled.ScheduledScript;
import io.liveoak.scripts.scheduled.manager.ScheduleManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.vertx.java.core.Vertx;

/**
 * Scripts to run based on an schedule.
 *
 * This is to script methods such as: at, until, cron/interval
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptsResource extends ScriptsResource implements RootResource {

    private String id;

    private final ScheduleManager scheduleManager;
    private File scriptDirectory;

    Resource parent;

    protected static final Logger log = Logger.getLogger("io.liveoak.scripts");


    // Configuration Keys
    private static final String DIRECTORY = "script-directory";
    private static final String TIMEOUT = "default-timeout";

    private Map<String, ScheduledScript> scripts = new ConcurrentHashMap<>();
    private ScriptConfig scriptConfig;

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    public ScheduledScriptsResource(String id, Vertx vertx, ScheduleManager scheduleManager) {
        super(vertx);
        this.id = id;
        this.scheduleManager = scheduleManager;
    }

    public ScheduleManager getScheduleManager() {
        return this.scheduleManager;
    }

    @Override
    public String id() {
        return id;
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
            String resourceBasedDir = getScriptConfig().getScriptDirectory();
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

    //TODO: move to common class
    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Object dirProperty = state.getProperty(DIRECTORY);
        if (dirProperty != null && dirProperty instanceof String) {
            ScriptConfig.Builder configBuilder = new ScriptConfig.Builder((String) dirProperty);

            Object timeout = state.getProperty(TIMEOUT);
            if (timeout != null && timeout instanceof Integer && (Integer)timeout >= 0) {
                configBuilder.setTimeout((Integer)timeout);
            } else if (timeout != null) {
                responder.invalidRequest("A '" + TIMEOUT + "' property must be a positive integer.");
                return;
            }

            this.scriptConfig = configBuilder.build();

            responder.resourceUpdated(this);
        } else {
            responder.invalidRequest("A '" + DIRECTORY + "' property is required and must be a String.");
        }
    }

    //TODO: move to common class
    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Object dirProperty = state.getProperty(DIRECTORY);
        if (dirProperty != null && dirProperty instanceof String) {
            if (!scriptConfig.getScriptDirectory().equals((String)dirProperty)) {
                responder.invalidRequest("A '" + DIRECTORY + "' property cannot be updated." );
                return;
            }
        } else {
            responder.invalidRequest("A '" + DIRECTORY + "' property is required and must be a String");
            return;
        }

        Object timeout = state.getProperty(TIMEOUT);
        if (timeout != null && timeout instanceof Integer && (Integer)timeout >= 0) {
            scriptConfig.setTimeout((Integer)timeout);
        } else if (timeout != null) {
            responder.invalidRequest("A '" + TIMEOUT + "' property must be a positive integer.");
            return;
        }

        responder.resourceUpdated(this);
    }

    //TODO: move to common class
    public ScriptConfig getScriptConfig() {
        return scriptConfig;
    }

    public Logger logger() {
        return log;
    }
}
