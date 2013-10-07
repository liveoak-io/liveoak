/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.responses.ResourceUpdatedResponse;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpUpdateResourceRequestEncoder extends MessageToMessageEncoder<ResourceUpdatedResponse> {

    private final Container container;

    public HttpUpdateResourceRequestEncoder(Container container) {
        this.container = container;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ResourceUpdatedResponse msg, List<Object> out) throws Exception {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ResourceCodec codec = container.getCodecManager().getResourceCodec(msg.getMimeType());
        ByteBuf encoded = codec.encode(msg.getResource());

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, encoded.readableBytes());
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, msg.getMimeType());
        response.content().writeBytes(encoded);
        out.add(response);
    }
}
