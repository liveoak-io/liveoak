/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.auth;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.*;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.function.Consumer;

/**
 * Handler for checking authorization of current request. It's independent of protocol.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    private final Client client;

    public AuthzHandler(Client client) {
        this.client = client;
    }

    private String getPrefix(ResourcePath path) {
        if (path.segments().size() < 2) {
            return null;
        }
        String prefix = "/" + path.head().name() + "/" + path.subPath().head().name();
        return prefix;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest req) throws Exception {
        String prefix = getPrefix(req.resourcePath());
        if (prefix == null) {
            ctx.fireChannelRead(req);
            return;
        }
        try {
            // Put current request as attribute of the request, which will be sent to AuthzService
            RequestAttributes attribs = new DefaultRequestAttributes();
            attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, req.requestContext());
            attribs.setAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, req.state());
            RequestContext authzRequest = new RequestContext.Builder().requestAttributes(attribs).build();

            client.read(authzRequest, prefix + "/authz/authzCheck", new Consumer<ClientResourceResponse>() {
                @Override
                public void accept(ClientResourceResponse resourceResponse) {
                    if (resourceResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                        ctx.fireChannelRead(req);
                        return;
                    }
                    try {
                        ResourceState state = resourceResponse.state();

                        boolean authorized = (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);

                        if (authorized) {
                            ctx.fireChannelRead(req);
                        } else {
                            boolean authenticated = req.requestContext().securityContext().isAuthenticated();
                            ResourceErrorResponse.ErrorType errorType = authenticated ? ResourceErrorResponse.ErrorType.FORBIDDEN : ResourceErrorResponse.ErrorType.NOT_AUTHORIZED;
                            ctx.writeAndFlush(new DefaultResourceErrorResponse(req, errorType));
                        }
                    } catch (Throwable t) {
                        ctx.writeAndFlush(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
                    }
                }
            });
        } catch (Throwable t) {
            ctx.writeAndFlush(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
        }
    }

}
