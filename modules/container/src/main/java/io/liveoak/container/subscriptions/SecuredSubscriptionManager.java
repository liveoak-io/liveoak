/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.container.subscriptions;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SecuredSubscriptionManager extends DefaultSubscriptionManager {

    public SecuredSubscriptionManager(Client client) {
        this.client = client;
    }

    @Override
    protected void subscribeResourceCreated(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        checkSecurity(path, subscription, resourceResponse, () -> {
            super.subscribeResourceCreated(path, subscription, resourceResponse);
        });
    }

    @Override
    protected void subscribeResourceUpdated(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        checkSecurity(path, subscription, resourceResponse, () -> {
            super.subscribeResourceUpdated(path, subscription, resourceResponse);
        });
    }

    @Override
    protected void subscribeResourceDeleted(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        checkSecurity(path, subscription, resourceResponse, () -> {
            super.subscribeResourceDeleted(path, subscription, resourceResponse);
        });
    }

    // TODO: Create common authorization helper to avoid have similar code for invoking authorization in 3 places
    protected void checkSecurity(ResourcePath path, Subscription subscription, ResourceResponse origResourceResponse, Runnable callback) {
        // Don't authorize subscriptions like UPSSubscription
        if (!subscription.isSecure()) {
            callback.run();
            return;
        }

        try {
            if (path.segments().size() < 0) {
                sendError(subscription, HttpResponseStatus.INTERNAL_SERVER_ERROR, origResourceResponse);
                return;
            }
            String applicationPrefix = path.segments().get(0).name();

            RequestAttributes attribs = new DefaultRequestAttributes();
            RequestContext reqContext = createRequestContext(path, subscription);
            attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqContext);

            // Use the state of the resource, which is sent to subscription
            attribs.setAttribute(AuthzConstants.ATTR_RESPONSE_RESOURCE_STATE, origResourceResponse.state());

            RequestContext authzRequest = new RequestContext.Builder().requestAttributes(attribs).build();

            client.read(authzRequest, applicationPrefix + "/authz/authzCheck", (ClientResourceResponse resourceResponse) -> {

                // Authorize automatically if Authz service is not available
                if (resourceResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                    callback.run();
                    return;
                }

                try {
                    ResourceState state = resourceResponse.state();
                    boolean authorized = (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);

                    if (authorized) {
                        callback.run();
                    } else {
                        boolean authenticated = subscription.securityContext().isAuthenticated();
                        HttpResponseStatus errorStatus = authenticated ? HttpResponseStatus.FORBIDDEN : HttpResponseStatus.UNAUTHORIZED;
                        sendError(subscription, errorStatus, origResourceResponse);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private RequestContext createRequestContext(ResourcePath resourcePath, Subscription subscription) {
        return new RequestContext.Builder()
                .requestType(RequestType.READ)
                .resourcePath(resourcePath)
                .securityContext(subscription.securityContext()).build();
    }

    private void sendError(Subscription subscription, HttpResponseStatus status, ResourceResponse origResourceResponse) throws Exception {
        ResourceState state = new DefaultResourceState(origResourceResponse.resource().id());
        state.putProperty("error", "New resource subscribed, but you can't see it due to error: " + status.reasonPhrase());
        subscription.sendAuthzError(state, origResourceResponse.resource(), status.code());
    }

    private final Client client;
}
