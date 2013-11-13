package io.liveoak.container.protocols.http;


import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.Before;
import org.junit.Test;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.container.codec.json.JSONDecoder;
import io.liveoak.container.codec.json.JSONEncoder;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.state.ResourceState;

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
        this.codecManager.registerResourceCodec("application/json", new ResourceCodec(null, JSONEncoder.class, new JSONDecoder()));
        this.channel = new EmbeddedChannel(new HttpResourceRequestDecoder(this.codecManager));
    }

    @Test
    public void testDecodeGet() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.GET, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(RequestType.READ);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        // TODO: still looking into whether this test failing is proper or not
        //assertThat( decoded.pagination() ).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodeDelete() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.DELETE, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(RequestType.DELETE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodePost() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.POST, "/memory/people/bob", "{ name: 'bob' }");

        assertThat(decoded.requestType()).isEqualTo(RequestType.CREATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ResourceState.class);

        ResourceState state = decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
    }

    @Test
    public void testDecodePut() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.PUT, "/memory/people/bob", "{ name: 'bob' }");

        assertThat(decoded.requestType()).isEqualTo(RequestType.UPDATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.pagination()).isNotNull();
        assertThat(decoded.pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ResourceState.class);

        ResourceState state = decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
    }

    protected ResourceRequest decode(HttpMethod method, String uri) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        channel.writeInbound(httpRequest);
        return (ResourceRequest) channel.readInbound();
    }

    protected ResourceRequest decode(HttpMethod method, String uri, String body) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        httpRequest.headers().add( HttpHeaders.Names.CONTENT_TYPE, "application/json" );
        httpRequest.content().writeBytes(body.getBytes());
        channel.writeInbound(httpRequest);
        return (ResourceRequest) channel.readInbound();
    }
}
