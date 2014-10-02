package io.liveoak.scripts.resource;

import io.liveoak.scripts.common.Script;
import io.liveoak.spi.exceptions.InvalidPropertyTypeException;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.RequiredPropertyException;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class ScriptResource implements Resource {

    // Property names common to all scripting resources
    protected static final String NAME = "name";
    protected static final String DESCRIPTION = "description";
    protected static final String ENABLED = "enabled";
    protected static final String LIBRARIES = "libraries";

    // The script associated with this resource
    protected abstract Script getScript();

    protected abstract void setScriptBuffer(ByteBuf buffer) throws Exception;

    protected ByteBuf getScriptBuffer() {
        return getScript().getScriptBuffer();
    }

    protected abstract void deleteMember(RequestContext ctx, String id, Responder responder);


    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(ScriptFileResource.ID) && getScriptBuffer()!= null) {
            responder.resourceRead(new ScriptFileResource(this));
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        readMembers(ctx, sink, true);
    }

    protected void readMembers(RequestContext ctx, ResourceSink sink, boolean close) throws Exception {
        if (getScriptBuffer() != null) {
            sink.accept(new ScriptFileResource(this));
        }
        if (close) {
            sink.close();
        }
    }

    //Essentially does the readProperties does, but does not close the sink.
    protected void sinkProperties(RequestContext ctx, PropertySink sink) throws Exception {
        Script script = getScript();
        sink.accept(NAME, script.getName());
        sink.accept(DESCRIPTION, script.getDescription());
        sink.accept(ENABLED, script.isEnabled());
        sink.accept(LIBRARIES, script.getLibraries());
    }

    //TODO: move this to a utility class or to the ResourceState class directly?
    protected Object getProperty(String name, ResourceState state, boolean required, Class<?> requestedType) throws PropertyException {
        return getProperty(name, state, required, requestedType, false);
    }

    //TODO: move this to a utility class or to the ResourceState class directly?
    protected Object getProperty(String name, ResourceState state, boolean required, Class<?> requestedType, boolean ignoreType) throws PropertyException {
        Object propertyObject = state.getProperty(name);
        if (required && propertyObject == null) {
            throw new RequiredPropertyException(name, requestedType);
        } else if (propertyObject == null) {
            return null;
        } else if (propertyObject.getClass() == requestedType) {
            return requestedType.cast(propertyObject);
        } else if (requestedType == Long.class && propertyObject.getClass() == Integer.class) {
            //special check for numbers
            //TODO: this should probably be done when we read in the file :/
            return new Long((Integer)propertyObject);
        } else if (ignoreType == false) {
            throw new InvalidPropertyTypeException(name, requestedType);
        } else {
            return null;
        }
    }
}
