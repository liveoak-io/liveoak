/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;

import io.liveoak.container.ResourceRequest;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.state.BinaryResourceState;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class HttpBinaryResourceRequestDecoderTest {

    private ResourceCodecManager codecManager;
    private EmbeddedChannel channel;

    @Before
    public void setUp() {
        this.codecManager = new ResourceCodecManager();
        this.channel = new EmbeddedChannel(new HttpResourceRequestDecoder(this.codecManager));
    }

    @Test
    public void testDecodePost() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.POST, "/memory/data", "Some text to be saved!");

        assertThat(decoded.requestType()).isEqualTo(RequestType.CREATE);

        assertThat(decoded.resourcePath().segments()).hasSize(2);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("data");

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(BinaryResourceState.class);

        BinaryResourceState state = (BinaryResourceState) decoded.state();

        assertThat(state.getBuffer().toString(Charset.defaultCharset())).isEqualTo("Some text to be saved!");
    }

    @Test
    public void testDecodePut() throws Exception {
        ResourceRequest decoded = decode(HttpMethod.PUT, "/memory/data/file", "Some updated text to be saved!");

        assertThat(decoded.requestType()).isEqualTo(RequestType.UPDATE);

        assertThat(decoded.resourcePath().segments()).hasSize(3);
        assertThat(decoded.resourcePath().segments().get(0)).isEqualTo("memory");
        assertThat(decoded.resourcePath().segments().get(1)).isEqualTo("data");
        assertThat(decoded.resourcePath().segments().get(2)).isEqualTo("file");

        assertThat(decoded.state()).isNotNull();
        assertThat(decoded.state()).isInstanceOf(BinaryResourceState.class);

        BinaryResourceState state = (BinaryResourceState) decoded.state();

        assertThat(state.getBuffer().toString(Charset.defaultCharset())).isEqualTo("Some updated text to be saved!");
    }

    protected ResourceRequest decode(HttpMethod method, String uri, String body) {
        FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
        httpRequest.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
        httpRequest.content().writeBytes(body.getBytes());
        channel.writeInbound(httpRequest);
        return (ResourceRequest) channel.readInbound();
    }
}
