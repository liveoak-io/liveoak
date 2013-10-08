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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.requests.UpdateResourceRequest;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpUpdateResourceRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {
    private final Container container;

    public HttpUpdateResourceRequestDecoder(Container container) {
        this.container = container;
    }

    //TODO: Refactor some of these decoders to reduce code dup.
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        ResourcePath path = new ResourcePath(request.getUri());
        if (path.isResourcePath() && request.getMethod() == HttpMethod.PUT) {
            //TODO: Probably need to support http chunks ?
            out.add(new UpdateResourceRequest(path.getType(), path.getCollectionName(), path.getResourceId(), request.content()));
        } else {
            ReferenceCountUtil.retain(request);
            out.add(request);
        }
    }
}
