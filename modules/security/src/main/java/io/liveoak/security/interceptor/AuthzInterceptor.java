/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.interceptor;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.*;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.liveoak.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Interceptor for checking authorization of current request. It's independent of protocol.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzInterceptor extends DefaultInterceptor {

    private final Client client;

    public AuthzInterceptor(Client client) {
        this.client = client;
    }

    private String getPrefix(ResourcePath path) {
        if (path.segments().size() < 1) {
            return null;
        }
        String prefix = "/" + path.head().name();
        return prefix;
    }

    @Override
    public void onInbound(InboundInterceptorContext ctx) throws Exception {
        ResourceRequest req = ctx.request();
        String prefix = getPrefix(req.resourcePath());
        if (prefix == null) {
            ctx.forward();
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
                        ctx.forward();
                        return;
                    }
                    try {
                        ResourceState state = resourceResponse.state();

                        boolean authorized = (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);

                        if (authorized) {
                            ctx.forward();
                        } else {
                            boolean authenticated = req.requestContext().securityContext().isAuthenticated();
                            ResourceErrorResponse.ErrorType errorType = authenticated ? ResourceErrorResponse.ErrorType.FORBIDDEN : ResourceErrorResponse.ErrorType.NOT_AUTHORIZED;
                            ctx.replyWith(new DefaultResourceErrorResponse(req, errorType));
                        }
                    } catch (Throwable t) {
                        ctx.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
                    }
                }
            });
        } catch (Throwable t) {
            ctx.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
        }
    }


    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        ResourceResponse response = context.response();
        if (context.request().requestType() == RequestType.READ && response.responseType() == ResourceResponse.ResponseType.READ && response.state() != null) {
            ResourcePath resourcePath = new ResourcePath(response.resource().uri().toString());
            SecurityContext securityContext = context.request().requestContext().securityContext();
            processState(resourcePath, response.state(), securityContext, new Consumer<ResourceState>() {

                @Override
                public void accept(ResourceState authorizedState) {
                    response.setState(authorizedState);
                    context.forward();
                }

            });
        } else {
            super.onOutbound(context);
        }
    }

    protected void processState(ResourcePath currentResourcePath, ResourceState resourceState, SecurityContext securityContext, Consumer<ResourceState> callback) {
        if (resourceState.members().isEmpty()) {
            callback.accept(resourceState);
            return;
        }

        // First check all, which are going to be counted
        final AtomicInteger pendingRequests = new AtomicInteger(resourceState.members().size());

        // Copy to avoid concurrent modification
        List<ResourceState> childResourcesCopy = new ArrayList<>(resourceState.members());

        for (ResourceState childState : childResourcesCopy) {
            if (!childState.getPropertyNames().isEmpty() || !childState.members().isEmpty()) {
                // Send authz request now
                ResourcePath childResourcePath = new ResourcePath(currentResourcePath);
                childResourcePath.appendSegment(childState.id());
                RequestContext reqToAuthorize = new RequestContext.Builder()
                        .resourcePath(childResourcePath)
                        .requestType(RequestType.READ)
                        .securityContext(securityContext);
                RequestAttributes attribs = new DefaultRequestAttributes();
                attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqToAuthorize);
                // Use the state of the resource, which is going to be returned
                attribs.setAttribute(AuthzConstants.ATTR_RESPONSE_RESOURCE_STATE, childState);
                RequestContext authzRequest = new RequestContext.Builder().requestAttributes(attribs).build();

                client.read(authzRequest, getPrefix(currentResourcePath) + "/authz/authzCheck", new Consumer<ClientResourceResponse>() {

                    @Override
                    public void accept(ClientResourceResponse authzResponse) {
                        boolean authorized = false;
                        if (authzResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                            authorized = true;
                        } else {
                            ResourceState state = authzResponse.state();
                            boolean authzResult = (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);
                            authorized = authzResult;
                        }

                        if (authorized) {
                            // Recursive call to check members of this one
                            processState(childResourcePath, childState, securityContext, new Consumer<ResourceState>() {

                                @Override
                                public void accept(ResourceState childState) {
                                    checkAuthzFinished(pendingRequests, resourceState, callback);
                                }

                            });
                        } else {
                            // TODO:
                            // logger.trace...
                            notAuthorized(resourceState, childState);
                            checkAuthzFinished(pendingRequests, resourceState, callback);
                        }
                    }
                });
            } else {
                checkAuthzFinished(pendingRequests, resourceState, callback);
            }
        }

    }

    private void checkAuthzFinished(AtomicInteger pendingRequests, ResourceState resourceState, Consumer<ResourceState> callback) {
        int current = pendingRequests.decrementAndGet();
        if (current == 0) {
            callback.accept(resourceState);
        }
    }

    protected void notAuthorized(ResourceState resourceState, ResourceState childState) {
        resourceState.members().remove(childState);
    }

}
