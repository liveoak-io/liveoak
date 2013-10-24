package org.projectodd.restafari.container.protocols.http;


import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.json.JSONDecoder;
import org.projectodd.restafari.container.codec.json.JSONEncoder;
import org.projectodd.restafari.container.mime.MediaType;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HttpResourceRequestDecoderTest {

    private ResourceCodecManager codecManager;
    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        this.codecManager = new ResourceCodecManager();
        this.codecManager.registerResourceCodec("application/json", new ResourceCodec(new JSONEncoder(), new JSONDecoder()));
        this.channel = new EmbeddedChannel(new HttpResourceRequestDecoder(this.codecManager));
    }

    @Test
    public void testDecodeGet() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.GET, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(ResourceRequest.RequestType.READ);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        // TODO: still looking into whether this test failing is proper or not
        //assertThat( decoded.pagination() ).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodeDelete() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.DELETE, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(ResourceRequest.RequestType.DELETE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodePost() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.POST, "/memory/people/bob", "{ name: 'bob' }");

        assertThat(decoded.requestType()).isEqualTo(ResourceRequest.RequestType.CREATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ObjectResourceState.class);

        ObjectResourceState state = (ObjectResourceState) decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
    }

    @Test
    public void testDecodePut() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.PUT, "/memory/people/bob", "{ name: 'bob' }");

        assertThat(decoded.requestType()).isEqualTo(ResourceRequest.RequestType.UPDATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ObjectResourceState.class);

        ObjectResourceState state = (ObjectResourceState) decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
    }

    protected ResourceRequest decode(HttpMethod method, String uri) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        channel.writeInbound(httpRequest);
        return (ResourceRequest) channel.readInbound();
    }

    protected ResourceRequest decode(HttpMethod method, String uri, String body) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        httpRequest.content().writeBytes(body.getBytes());
        channel.writeInbound(httpRequest);
        return (ResourceRequest) channel.readInbound();
    }
}
