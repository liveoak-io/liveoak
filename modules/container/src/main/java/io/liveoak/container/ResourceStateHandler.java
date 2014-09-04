/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.concurrent.Executor;

import io.liveoak.client.impl.ClientResourceResponseImpl;
import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.codec.driver.RootEncodingDriver;
import io.liveoak.common.codec.state.ResourceStateEncoder;
import io.liveoak.container.protocols.RequestCompleteEvent;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ResourceStateHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(ResourceStateHandler.class);

    private Executor workerPool;

    public ResourceStateHandler(Executor workerPool) {
        this.workerPool = workerPool;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ResourceResponse && !(msg instanceof ResourceErrorResponse)) {
            ResourceResponse response = (ResourceResponse) msg;
            Runnable action = () -> {
                encode(ctx, response, promise);
            };

            if (response.resource() instanceof BlockingResource) {
                this.workerPool.execute(action);
            } else {
                action.run();
            }

        } else {
            super.write(ctx, msg, promise);
        }
    }

    /**
     * Encode (for some cheap value of 'encode') a resulting resource into a ResourceState.
     *
     * @param ctx
     * @param response The response to encode.
     * @throws Exception
     */
    protected void encode(ChannelHandlerContext ctx, ResourceResponse response, ChannelPromise promise) {
        final ClientResourceResponse.ResponseType responseType = ClientResourceResponse.ResponseType.OK;
        if (response.resource() == null) {
            ctx.writeAndFlush(new ClientResourceResponseImpl(response.inReplyTo(), responseType, response.inReplyTo().resourcePath().toString(), null));
            ctx.fireUserEventTriggered(new RequestCompleteEvent(response.requestId()));
            return;
        }

        final ResourceStateEncoder encoder = new ResourceStateEncoder();

        RootEncodingDriver driver = new RootEncodingDriver(response.inReplyTo().requestContext(), encoder, response.resource(), () -> {
            ResourceState state = encoder.root();
            response.setState(state);
            ctx.writeAndFlush(response, promise);
        }, t -> handleError(ctx, response, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, t));

        try {
            driver.encode();
        } catch (Exception e) {
            handleError(ctx, response, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, e);
        }
    }

    private void handleError(ChannelHandlerContext ctx, ResourceResponse response, ResourceErrorResponse.ErrorType errorType, Throwable t) {
        log.error(errorType, t);
        ctx.writeAndFlush(new DefaultResourceErrorResponse(response.inReplyTo(), errorType, t.getMessage(), t));
        ctx.fireUserEventTriggered(new RequestCompleteEvent(response.requestId()));
    }
}
