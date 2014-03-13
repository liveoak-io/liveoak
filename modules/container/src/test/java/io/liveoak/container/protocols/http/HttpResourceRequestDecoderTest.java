/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;


import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;

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
        this.codecManager.registerResourceCodec(MediaType.JSON, new ResourceCodec(JSONEncoder.class, new JSONDecoder()));
        this.channel = new EmbeddedChannel(new HttpResourceRequestDecoder(this.codecManager));
    }

    @Test
    public void testDecodeGet() throws Exception {
        DefaultResourceRequest decoded = decode(HttpMethod.GET, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(RequestType.READ);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0).name()).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1).name()).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2).name()).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.requestContext().pagination()).isNotNull();
        // TODO: still looking into whether this test failing is proper or not
        //assertThat( decoded.pagination() ).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodeDelete() throws Exception {
        DefaultResourceRequest decoded = decode(HttpMethod.DELETE, "/memory/people/bob");

        assertThat(decoded.requestType()).isEqualTo(RequestType.DELETE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0).name()).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1).name()).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2).name()).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.requestContext().pagination()).isNotNull();
        assertThat(decoded.requestContext().pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNull();
    }

    @Test
    public void testDecodePost() throws Exception {
        DefaultResourceRequest decoded = decode(HttpMethod.POST, "/memory/people/bob", "{ name: 'bob', int: 1024, maxInt: 2147483647, minInt: -2147483648, longNeg: -2147483659, longPos: 2147483648 }");

        assertThat(decoded.requestType()).isEqualTo(RequestType.CREATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0).name()).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1).name()).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2).name()).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.requestContext().pagination()).isNotNull();
        assertThat(decoded.requestContext().pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ResourceState.class);

        ResourceState state = decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
        assertThat(state.getProperty("int")).isEqualTo(1024);
        assertThat(state.getProperty("maxInt")).isEqualTo(2147483647);
        assertThat(state.getProperty("minInt")).isEqualTo(-2147483648);
        assertThat(state.getProperty("longNeg")).isEqualTo(-2147483659L);
        assertThat(state.getProperty("longPos")).isEqualTo(2147483648L);
    }

    @Test
    public void testDecodePut() throws Exception {
        DefaultResourceRequest decoded = decode(HttpMethod.PUT, "/memory/people/bob", "{ name: 'bob', int: 1024, maxInt: 2147483647, minInt: -2147483648, longNeg: -2147483659, longPos: 2147483648  }");

        assertThat(decoded.requestType()).isEqualTo(RequestType.UPDATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0).name()).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1).name()).isEqualTo("people");
        assertThat(decoded.resourcePath().segments().get(2).name()).isEqualTo("bob");

        //assertThat(decoded.mediaType()).isEqualTo(MediaType.JSON);

        assertThat(decoded.requestContext().pagination()).isNotNull();
        assertThat(decoded.requestContext().pagination()).isEqualTo(Pagination.NONE);

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(ResourceState.class);

        ResourceState state = decoded.state();

        assertThat(state.getProperty("name")).isEqualTo("bob");
        assertThat(state.getProperty("int")).isEqualTo(1024);
        assertThat(state.getProperty("maxInt")).isEqualTo(2147483647);
        assertThat(state.getProperty("minInt")).isEqualTo(-2147483648);
        assertThat(state.getProperty("longNeg")).isEqualTo(-2147483659L);
        assertThat(state.getProperty("longPos")).isEqualTo(2147483648L);
    }

    protected DefaultResourceRequest decode(HttpMethod method, String uri) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        channel.writeInbound(httpRequest);
        return (DefaultResourceRequest) channel.readInbound();
    }

    protected DefaultResourceRequest decode(HttpMethod method, String uri, String body) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/memory/people/bob");
        httpRequest.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        httpRequest.content().writeBytes(body.getBytes());
        channel.writeInbound(httpRequest);
        return (DefaultResourceRequest) channel.readInbound();
    }
}
