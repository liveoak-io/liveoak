package io.liveoak.spi.resource.async;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ResourceTest {

    @Test
    public void testRootUri() {
        Resource r = new MockResource("");
        assertThat(r.uri().toString()).isEqualTo("/");
    }

    @Test
    public void testLongerUri() {
        Resource r = new MockResource(new MockResource(new MockResource("foo"), "bar"), "baz");
        assertThat(r.uri().toString()).isEqualTo("/foo/bar/baz");
    }

    @Test
    public void testRootUriWithMatrix() {
        Resource r = new MockResource( new MockResource(""), ";config" );
        assertThat(r.uri().toString()).isEqualTo("/;config");
    }

    @Test
    public void testLongerUriWithMatrix() {
        Resource r = new MockResource(new MockResource(new MockResource("foo"), "bar"), ";config");
        assertThat(r.uri().toString()).isEqualTo("/foo/bar;config");
    }

    public static class MockResource implements Resource {

        public MockResource(Resource parent, String id) {
            this.parent = parent;
            this.id = id;
        }

        public MockResource(String id) {
            this.id = id;
        }

        @Override
        public Resource parent() {
            return this.parent;
        }

        @Override
        public String id() {
            return this.id;
        }

        private Resource parent;
        private String id;
    }
}
