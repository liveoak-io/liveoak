package io.liveoak.scripts.resourcetriggered.resource;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.scripts.common.GenericScriptResource;
import io.liveoak.spi.InvalidPropertyTypeException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.LazyResourceState;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScript extends GenericScriptResource implements Comparable<ResourceScript> {

    private String target;
    private Integer priority = 1; //set the default priority to 1

    protected static final String TARGET_PATH = "resource-path";
    protected static final String PRIORITY = "priority";

    protected ScriptFileResource scriptFileResource;

    private ResourceScripts parent;

    protected ResourceScript(ResourceScripts parent, String id, ResourceState state) throws Exception {
        super(parent, id, state);
        this.parent = parent;
    }

    protected static ResourceScript generate(ResourceScripts parent, String id, ResourceState state) throws Exception {
        ResourceScript serverScriptResource = new ResourceScript(parent, id, state);

        Object target = state.getProperty(TARGET_PATH);
        if (target != null && target instanceof String) {
            serverScriptResource.target = (String) target;
        } else {
            throw new InvalidPropertyTypeException(TARGET_PATH, String.class);
        }

        Object priority = state.getProperty(PRIORITY);
        if (priority != null && priority instanceof Integer) {
            serverScriptResource.priority = (Integer) priority;
        } else if (priority != null) {
            throw new InvalidPropertyTypeException(PRIORITY, Integer.class);
        }

        return serverScriptResource;
    }

    protected static ResourceScript generate(ResourceScripts parent, ResourceState state) throws Exception {
        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        ResourceScript serverScriptResource = new ResourceScript(parent, id, state);

        Object target = state.getProperty(TARGET_PATH);
        if (target != null && target instanceof String) {
            serverScriptResource.target = (String) target;
        } else {
            throw new InvalidPropertyTypeException(TARGET_PATH, String.class);
        }

        Object priority = state.getProperty(PRIORITY);
        if (priority != null && priority instanceof Integer) {
            serverScriptResource.priority = (Integer) priority;
        } else if (priority != null) {
            throw new InvalidPropertyTypeException(PRIORITY, Integer.class);
        }

        return serverScriptResource;
    }

    public String target() {
        return target;
    }

    public Integer priority() {
        return priority;
    }

    @Override
    public int compareTo(ResourceScript resourceServerScriptResource) {
        if (this.priority() > resourceServerScriptResource.priority()) {
            return 1;
        } else if (this.priority() == resourceServerScriptResource.priority()) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(NAME, name);
        sink.accept(DESCRIPTION, description);
        sink.accept(ENABELD, enabled);
        sink.accept(TARGET_PATH, target);
        sink.accept(PRIORITY, priority);
        sink.accept(LIBRARIES, libraries);

        if (scriptFileResource != null) {
            sink.accept("provides", scriptFileResource.provides());
        }

        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        //TODO: put this code in a common location since its also used when creating
        Object nameProperty = state.getProperty(NAME);
        if (nameProperty != null) {
            name = nameProperty.toString();
        } else {
            name = null;
        }

        Object descriptionProperty = state.getProperty(DESCRIPTION);
        if (descriptionProperty != null) {
            description = descriptionProperty.toString();
        } else {
            description = null;
        }

        Object enabledProperty = state.getProperty(ENABELD);
        if (enabledProperty != null && enabledProperty instanceof Boolean) {
            enabled = (Boolean) enabledProperty;
        } else {
            enabled = false;
        }

        libraries.clear();
        Object libraries = state.getProperty(LIBRARIES);
        if (libraries != null && libraries instanceof List) {
            for (Object entry : (List) libraries) {
                if (entry instanceof String) {
                    this.libraries.add((String) entry);
                }
            }
        } else if (libraries != null) {
            throw new InvalidPropertyTypeException(LIBRARIES, List.class);
        }

        Object targetProperty = state.getProperty(TARGET_PATH);
        if (targetProperty != null && targetProperty instanceof String) {
            target = (String) targetProperty;
        } else {
            throw new InvalidPropertyTypeException(TARGET_PATH, String.class);
        }

        Object priorityProperty = state.getProperty(PRIORITY);
        if (priorityProperty != null && priorityProperty instanceof Integer) {
            priority = (Integer) priorityProperty;
        } else if (priorityProperty != null) {
            throw new InvalidPropertyTypeException(PRIORITY, Integer.class);
        }

        parent.getResourceInterceptorManager().addResource(this);
        parent.writeMetadataFile(this.id(), ConversionUtils.convert(state));
        responder.resourceUpdated(this);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        if (state instanceof LazyResourceState) {
            LazyResourceState lazyResourceState = (LazyResourceState) state;
            ByteBuf content = lazyResourceState.contentAsByteBuf();
            setScriptFile(content.copy());

            parent.writeSourceFile(this.id(), content.copy());

            responder.resourceCreated(scriptFileResource);
            return;
        }

        responder.createNotSupported(this);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(scriptFileResource);
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (scriptFileResource != null && id.equals(scriptFileResource.id())) {
            responder.resourceRead(scriptFileResource);
        } else {
            responder.noSuchResource(id);
        }

    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteMember(ctx, this.id(), responder);
    }

    public void deleteMember(RequestContext ctx, String id, Responder responder) throws Exception {
        parent.getResourceInterceptorManager().removeResource(this.id);
        parent.deleteSourceFile(this.id);

        responder.resourceDeleted(this.scriptFileResource);

        this.scriptFileResource = null;
    }

    public ScriptFileResource getScriptFile() {
        return this.scriptFileResource;
    }

    public void setScriptFile(ByteBuf buffer) {
        this.scriptFileResource = new ScriptFileResource(this, buffer);
        parent.getResourceInterceptorManager().addResource(this);
    }


    public static class ReverseComparator implements Comparator<ResourceScript> {

        @Override
        public int compare(ResourceScript o1, ResourceScript o2) {
            if (o1.priority() > o2.priority()) {
                return -1;
            } else if (o1.priority() == o2.priority()) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}

