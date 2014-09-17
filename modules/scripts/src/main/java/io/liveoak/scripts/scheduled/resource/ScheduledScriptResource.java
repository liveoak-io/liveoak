package io.liveoak.scripts.scheduled.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.liveoak.scripts.resource.ScriptFileResource;
import io.liveoak.scripts.resource.ScriptResource;
import io.liveoak.scripts.scheduled.ScheduledScript;
import io.liveoak.spi.InvalidPropertyTypeException;
import io.liveoak.spi.PropertyException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScheduledScriptResource extends ScriptResource {

    // Property names specified in the scheduled script's state
    protected static final String AT = "at";
    protected static final String UNTIL = "until";
    protected static final String CRON = "cron";

    ScheduledScriptsResource parent;

    protected ScheduledScript script;

    public ScheduledScriptResource(ScheduledScriptsResource parent, ResourceState state) throws Exception {
        this.parent = parent;
        this.script = generateScript(state);
    }

    public ScheduledScriptResource(ScheduledScriptsResource parent, ScheduledScript script) {
        this.parent = parent;
        this.script = script;
    }

    private ScheduledScript generateScript(ResourceState state) throws PropertyException{
        String id = state.id();
        if (id == null) {
            if (script != null) {
                id = script.getId();
            } else {
                id = UUID.randomUUID().toString();
            }
        }

        //TODO: see if we can move the Name, Description, Enabled, & Libraries to the parent class.
        ScheduledScript.Builder builder = new ScheduledScript.Builder(id);

        String name = (String) getProperty(NAME, state, false, String.class);
        if (name != null) {
            builder.setName(name);
        }

        String description = (String)getProperty(DESCRIPTION, state, false, String.class);
        if (description != null) {
            builder.setDescription(description);
        }

        Boolean enabled = (Boolean) getProperty(ENABLED, state, false, Boolean.class);
        if (enabled != null) {
            builder.setEnabled(enabled);
        }

        List librariesProperty = (List) getProperty(LIBRARIES, state, false, ArrayList.class);
        if (librariesProperty != null) {
            List<String> libraries = new ArrayList<String>(librariesProperty.size());
            for (Object libName: librariesProperty) {
                if (libName instanceof String) {
                    libraries.add((String)libName);
                } else {
                    throw new InvalidPropertyTypeException(LIBRARIES, String.class, true);
                }
            }
            builder.setLibraries(libraries);
        }

        String atString = (String) getProperty(AT, state, false, String.class, true);
        if (atString != null) {
            //TODO: figure out how to handle the first-run/install situation. LIVEOAK-535
            // We could use something like a ${FIRST-RUN} variable and then set it to the time when its
            // originally installed, writing this new value back to the configuration option.
            if (atString.equals("first-run")){
                builder.setAt(new Date());
            }
        }

        Long atLong = (Long) getProperty(AT, state, false, Long.class, true);
        if (atLong != null) {
            builder.setAt(new Date(atLong));
        }

        Long untilLong = (Long) getProperty(UNTIL, state, false, Long.class);
        if (untilLong != null) {
            builder.setUntil(new Date(untilLong));
        }

        String cron = (String) getProperty(CRON, state, false, String.class);
        if (cron != null) {
            builder.setCron(cron);
        }

        return builder.build();
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return script.getId();
    }

    @Override
    public ScheduledScript getScript() {
        return this.script;
    }

    @Override
    protected void setScriptBuffer(ByteBuf buffer) throws Exception {
        script.setScriptBuffer(buffer);
        if (buffer != null) {
            parent.getScheduleManager().handleScript(this.getScript());
        }
    }

    @Override
    protected void deleteMember(RequestContext ctx, String id, Responder responder) {
        if (id == ScriptFileResource.ID && script.getScriptBuffer() != null) {
            Resource resource = new ScriptFileResource(this);
            this.script.setScriptBuffer(null);
            responder.resourceDeleted(resource);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        super.sinkProperties(ctx, sink);

        Date at = script.getAt();
        if (at != null) {
            sink.accept(AT, script.getAt().getTime());
        }

        Date until = script.getUntil();
        if (until != null) {
            sink.accept(UNTIL, script.getUntil().getTime());
        }

        sink.accept(CRON, script.getCron());
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        // call the parent readMembers but don't close the stream since we have an extra ContextResource to add.
        super.readMembers(ctx, sink, false);

        sink.accept(new ScriptContextResource(this, parent.getScheduleManager().getScheduler()));

        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(ScriptFileResource.ID) && getScriptBuffer()!= null) {
            responder.resourceRead(new ScriptFileResource(this));
        } else if (id.equals(ScriptContextResource.ID)) {
            responder.resourceRead(new ScriptContextResource(this, parent.getScheduleManager().getScheduler()));
        } else {
            responder.noSuchResource(id);
        }
    }
}
