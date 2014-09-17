package io.liveoak.scripts.resource;

import io.liveoak.scripts.endpoints.EndpointScripts;
import io.liveoak.scripts.libraries.resources.ScriptLibraries;
import io.liveoak.scripts.resourcetriggered.resource.ResourceScripts;
import io.liveoak.scripts.scheduled.resource.ScheduledScriptsResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptsRootResource implements RootResource {

    private Resource parent;
    private String id;

    ResourceScripts resourceTriggeredScripts;
    EndpointScripts endpointScripts;
    ScheduledScriptsResource scheduledScripts;
    ScriptLibraries scriptLibraries;

    protected static final Logger log = Logger.getLogger("io.liveoak.scripts");


    // Configuration Keys
    private static final String DIRECTORY = "script-directory";

    private String scriptDirectory;

    public ScriptsRootResource(String id, ScriptLibraries scriptLibraries, ResourceScripts resourceTriggeredScripts, ScheduledScriptsResource scheduledScripts) {
        this.resourceTriggeredScripts = resourceTriggeredScripts;
        this.resourceTriggeredScripts.parent(this);

        this.scriptLibraries = scriptLibraries;
        scriptLibraries.parent(this);

        this.endpointScripts = new EndpointScripts(this);

        this.scheduledScripts = scheduledScripts;
        this.scheduledScripts.parent(this);

        this.id = id;

    }

    @Override
    public void start() throws Exception {
        resourceTriggeredScripts.start();
        scheduledScripts.start();
    }


    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "LiveOak Scripts");
        sink.accept("description", "Manages server side scripts for the application.");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(resourceTriggeredScripts);
        sink.accept(endpointScripts);
        sink.accept(scheduledScripts);
        sink.accept(scriptLibraries);
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(resourceTriggeredScripts.id())) {
            responder.resourceRead(this.resourceTriggeredScripts);
        } else if (id.equals(endpointScripts.id())) {
            responder.resourceRead(this.endpointScripts);
        } else if (id.equals(scheduledScripts.id())) {
            responder.resourceRead(scheduledScripts);
        } else if (id.equals(scriptLibraries.id())) {
            responder.resourceRead(scriptLibraries);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Object dirProperty = state.getProperty(DIRECTORY);
        if (dirProperty != null && dirProperty instanceof String) {
            this.scriptDirectory = (String) dirProperty;
            responder.resourceUpdated(this);
        } else {
            responder.invalidRequest("A '" + DIRECTORY + "' property is required and must be a String.");
        }
    }

    public String getScriptDirectory() {
        return scriptDirectory;
    }

    public Logger logger() {
        return log;
    }
}
