package io.liveoak.scripts.objects.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResource implements Resource {

    ResourceState state;
    Map<String, Object> properties;
    List<Resource> members;

    public LiveOakResource() {
        this.state = new DefaultResourceState();
        this.properties = new HashMap<>();
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
    public String getID() {
        return state.id();
    }

    @Override
    public void setID(String id) {
        state.id(id);
    }

    @Override
    public String getURI() {
        if (state.uri() == null) {
            return null;
        } else {
            return state.uri().toString();
        }
    }

    @Override
    public void setURI(String uri) throws Exception {
        state.uri(new URI(uri));
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public List<Resource> getMembers() {
        return members;
    }

}