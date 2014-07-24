package io.liveoak.scripts.common;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.InvalidPropertyTypeException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class GenericScriptResource implements Resource {

    protected Resource parent;
    protected String id;

    protected String name = null;
    protected String description = null;
    protected Boolean enabled = false; //default to not enabled
    protected List<String> libraries = new ArrayList<>();

    protected static final String NAME = "name";
    protected static final String DESCRIPTION = "description";
    protected static final String ENABELD = "enabled";
    protected static final String LIBRARIES = "libraries";

    protected GenericScriptResource(Resource parent, String id, ResourceState resourceState) throws Exception {
        this.id = id;
        this.parent = parent;
        initialize(resourceState);
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    protected void initialize(ResourceState state) throws Exception {
        Object name = state.getProperty(NAME);
        if (name != null && name instanceof String) {
            this.name = (String) name;
        } else if (name != null) {
            throw new InvalidPropertyTypeException(NAME, String.class);
        }

        Object description = state.getProperty(DESCRIPTION);
        if (description != null && description instanceof String) {
            this.description = (String) description;
        } else if (description != null) {
            throw new InvalidPropertyTypeException(DESCRIPTION, String.class);
        }

        Object enabled = state.getProperty(ENABELD);
        if (enabled != null && enabled instanceof Boolean) {
            this.enabled = (Boolean) enabled;
        } else if (enabled != null) {
            throw new InvalidPropertyTypeException(ENABELD, Boolean.class);
        }

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
    }

    public String name() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean enabled() {
        return enabled;
    }

    public List<String> libraries() {
        return libraries;
    }
}
