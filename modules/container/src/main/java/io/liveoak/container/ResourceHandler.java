/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.concurrent.Executor;

import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.container.traversal.TraversingResponder;
import io.liveoak.spi.ResourceRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ResourceHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    public ResourceHandler(GlobalContext globalContext, Executor workerPool) {
        this.globalContext = globalContext;
        this.workerPool = workerPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest msg) throws Exception {
        ResourceRequest requestWrapper = globalContext.generateResourceRequest(msg);
        new TraversingResponder(this.workerPool, this.globalContext, requestWrapper, ctx).resourceRead(globalContext);
    }

    private GlobalContext globalContext;
    private Executor workerPool;

}
