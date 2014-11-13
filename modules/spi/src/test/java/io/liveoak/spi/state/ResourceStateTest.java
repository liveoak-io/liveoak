package io.liveoak.spi.state;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ResourceStateTest {

    @Test
    public void emptyMembers() throws Exception {
        ResourceState state = new MyResourceState();
        assertThat(state.member("test")).isNull();
    }

    @Test
    public void memberNotPresent() throws Exception {
        ResourceState state = new MyResourceState();
        state.addMember(new MyResourceState("test"));
        assertThat(state.member("test1")).isNull();
    }

    @Test
    public void memberPresent() throws Exception {
        ResourceState state = new MyResourceState();
        state.addMember(new MyResourceState("test"));
        assertThat(state.member("test")).isNotNull();
    }

    @Test
    public void memberPresentAmongstMany() throws Exception {
        ResourceState state = new MyResourceState();
        state.addMember(new MyResourceState("test"));
        state.addMember(new MyResourceState("tesing"));
        state.addMember(new MyResourceState("another"));
        assertThat(state.member("test")).isNotNull();
    }

    @Test
    public void nullId() throws Exception {
        ResourceState state = new MyResourceState();
        state.addMember(new MyResourceState("test"));
        assertThat(state.member(null)).isNull();
    }

    @Test
    public void emptyId() throws Exception {
        ResourceState state = new MyResourceState();
        state.addMember(new MyResourceState("test"));
        assertThat(state.member("")).isNull();
    }

    public static class MyResourceState implements ResourceState {
        public MyResourceState() {
        }

        public MyResourceState(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public void id(String id) {
            this.id = id;
        }

        @Override
        public void uri(URI uri) {
        }

        @Override
        public void putProperty(String name, Object value) {
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public Object removeProperty(String name) {
            return null;
        }

        @Override
        public Set<String> getPropertyNames() {
            return null;
        }

        @Override
        public void addMember(ResourceState member) {
            this.members.add(member);
        }

        @Override
        public List<ResourceState> members() {
            return this.members;
        }

        private String id;
        private List<ResourceState> members = new ArrayList<>();
    }
}
