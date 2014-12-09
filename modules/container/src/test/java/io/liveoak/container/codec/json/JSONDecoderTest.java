package io.liveoak.container.codec.json;

import java.io.IOException;

import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class JSONDecoderTest {

    protected ResourceState decode(String string) throws Exception {
        ByteBuf byteBuf = Unpooled.copiedBuffer(string.getBytes());

        JSONDecoder decoder = new JSONDecoder();
        ResourceState resourceState = decoder.decode(byteBuf);

        return resourceState;
    }

    @Test
    public void testDecodeResourceReference() throws Exception {

        String value = "{ id: 'foo', 'A': 1, 'bar': { 'id': 'helloworld','self': {href: '/foo/bar'}, 'A': 123, 'B': 'XYZ', 'baz': {'self':{href:'/foo/bar/baz'}}}}";

        ResourceState resourceState = decode(value);
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("foo");
        assertThat(resourceState.getPropertyNames().size()).isEqualTo(2);
        assertThat(resourceState.getProperty("A")).isEqualTo(1);

        ResourceState barState = resourceState.getProperty("bar", true, ResourceState.class);
        assertThat(barState.id()).isEqualTo("helloworld");
        assertThat(barState.uri().toString()).isEqualTo("/foo/bar");
        assertThat(barState.getPropertyNames().size()).isEqualTo(3);
        assertThat(barState.getProperty("A")).isEqualTo(123);
        assertThat(barState.getProperty("B")).isEqualTo("XYZ");

        ResourceState baz = barState.getProperty("baz", true, ResourceState.class);
        assertThat(baz.id()).isNull();
        assertThat(baz.uri().toString()).isEqualTo("/foo/bar/baz");
        assertThat(baz.getPropertyNames().size()).isEqualTo(0);
    }

    @Test
    public void testDecodeResourceMemberReference() throws Exception {

        String value = "{ id: 'foo', members: [{id: 'bar', self: {href: '/foo/bar'}}]}";

        ResourceState resourceState = decode(value);
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("foo");
        assertThat(resourceState.getPropertyNames().size()).isEqualTo(0);
        assertThat(resourceState.members().size()).isEqualTo(1);

        ResourceState memberState = resourceState.member("bar");
        assertThat(memberState.id()).isEqualTo("bar");
        assertThat(memberState.uri().toString()).isEqualTo("/foo/bar");
        assertThat(memberState.getPropertyNames().size()).isEqualTo(0);
    }

    @Test
    public void testInvalidMemberValue() throws Exception {
        // members must be an array
        String value = "{ id: 'foo', members: {id: 'bar', self: {href: '/foo/bar'}}}";
        try {
            ResourceState resourceState = decode(value);
            fail();
        } catch (IOException e) {
            // expected
        }
    }

}
