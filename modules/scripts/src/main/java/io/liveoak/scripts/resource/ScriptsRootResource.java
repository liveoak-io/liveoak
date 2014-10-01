package io.liveoak.scripts.resource;

import io.liveoak.scripts.libraries.resources.ScriptLibraries;
import io.liveoak.scripts.resourcetriggered.resource.ResourceScripts;
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
 * @author Ken Finnigan
 */
public class ScriptsRootResource implements RootResource {

    private Resource parent;
    private String id;

    ResourceScripts resourceTriggeredScripts;
    ScriptLibraries scriptLibraries;

    protected static final Logger log = Logger.getLogger("io.liveoak.scripts");


    // Configuration Keys
    private static final String DIRECTORY = "script-directory";
    private static final String TIMEOUT = "default-timeout";

    private ScriptConfig scriptConfig;

    public ScriptsRootResource(String id, ScriptLibraries scriptLibraries, ResourceScripts resourceTriggeredScripts) {
        this.resourceTriggeredScripts = resourceTriggeredScripts;
        this.resourceTriggeredScripts.parent(this);

        this.scriptLibraries = scriptLibraries;
        scriptLibraries.parent(this);

        this.id = id;

    }

    @Override
    public void start() throws Exception {
        resourceTriggeredScripts.start();
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
        sink.accept(DIRECTORY, this.scriptConfig.getScriptDirectory());
        sink.accept(TIMEOUT, this.scriptConfig.getTimeout());
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            sink.accept(resourceTriggeredScripts);
            sink.accept(scriptLibraries);
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.close();
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(resourceTriggeredScripts.id())) {
            responder.resourceRead(this.resourceTriggeredScripts);
        } else if (id.equals(scriptLibraries.id())) {
            responder.resourceRead(scriptLibraries);
        } else {
            responder.noSuchResource(id);
        }
    }

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

    public ScriptConfig getScriptConfig() {
        return scriptConfig;
    }

    public Logger logger() {
        return log;
    }
}
