package io.liveoak.scripts.objects.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResource implements Resource {

    ResourceState state;
    LiveOakStateProperties properties;
    List<Resource> members;

    public LiveOakResource() {
        this.state = new DefaultResourceState();
        this.properties = new LiveOakStateProperties();
        this.members = new ArrayList<>();
    }

    public LiveOakResource(String id) {
        this.state = new DefaultResourceState(id);
        this.properties = new LiveOakStateProperties();
        this.members = new ArrayList<>();
    }

    public LiveOakResource(ResourceState state) {
        this.state = state;
        this.properties = new LiveOakStateProperties(state);

        members = new ArrayList();
        for (ResourceState member : state.members()) {
            members.add(new LiveOakResource(member));
        }
    }

    @Override
    public String getId() {
        return state.id();
    }

    @Override
    public void setId(String id) {
        state.id(id);
    }

    @Override
    public String getUri() {
        if (state.uri() == null) {
            return null;
        } else {
            return state.uri().toString();
        }
    }

    @Override
    public void setUri(String uri) throws Exception {
        state.uri(new URI(uri));
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties (Map<String, Object> properties) {
        //TODO: remove this section once DynJS implements .entrySet

        Set<String> propertyNames = new HashSet(state.getPropertyNames());
        for (String propertyName : propertyNames) {
            state.removeProperty(propertyName);
        }

        this.properties.clear();
        for (String key: properties.keySet()) {
            this.properties.put(key, properties.get(key));
        }
    }

    @Override
    public Resource[] getMembers() {
        return members.toArray(new Resource[members.size()]);
    }
}