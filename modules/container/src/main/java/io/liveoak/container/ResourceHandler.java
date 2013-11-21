/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.responders.CreateResponder;
import io.liveoak.container.responders.DeleteResponder;
import io.liveoak.container.responders.ReadResponder;
import io.liveoak.container.responders.UpdateResponder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ResourceHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    public ResourceHandler(DefaultContainer container) {
        this.container = container;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest msg) throws Exception {
        String firstSegment = msg.resourcePath().head();

        switch (msg.requestType()) {
            case CREATE:
                new CreateResponder(container.workerPool(), container, msg, ctx).doRead(firstSegment, container);
                break;
            case READ:
                new ReadResponder(container.workerPool(), container, msg, ctx).doRead(firstSegment, container);
                break;
            case UPDATE:
                new UpdateResponder(container.workerPool(), container, msg, ctx).doRead(firstSegment, container);
                break;
            case DELETE:
                new DeleteResponder(container.workerPool(), container, msg, ctx).doRead(firstSegment, container);
                break;
        }
    }

    private DefaultContainer container;

}
