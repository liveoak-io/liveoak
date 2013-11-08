/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.integration;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.container.auth.AuthzConstants;
import io.liveoak.container.auth.SimpleLogger;
import io.liveoak.security.spi.AuthzDecision;
import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzCheckResource implements Resource {

    private static final Logger log = Logger.getLogger(AuthzCheckResource.class);

    private final String id;
    private final AuthzServiceRootResource parent;
    private List<AuthzPolicyEntry> policies;

    public AuthzCheckResource(String id, List<AuthzPolicyEntry> policies, AuthzServiceRootResource parent) {
        this.id = id;
        this.parent = parent;
        this.policies = policies;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        try {
            PolicyHandler handler = new PolicyHandler(ctx, sink);
            handler.next();
        } catch (Throwable t) {
            log.error("Failed to authorize request", t);
            sink.accept(AuthzConstants.ATTR_AUTHZ_RESULT, false);
            sink.close();
        }
    }

    private class PolicyHandler implements Consumer<ClientResourceResponse> {

        private final Client client;
        private final Queue<AuthzPolicyEntry> queue;

        private PropertySink sink;
        private RequestContext ctxToAuthorize;

        private AuthzDecision decision = AuthzDecision.IGNORE;

        public PolicyHandler(RequestContext ctx, PropertySink sink) {
            this.client = parent.getClient();
            this.sink = sink;

            ctxToAuthorize = ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class);
            queue = getPolicies(ctxToAuthorize.resourcePath());

            if (queue.isEmpty()) {
                decision = AuthzDecision.ACCEPT; // TODO This is temporary
            }
        }

        @Override
        public void accept(ClientResourceResponse response) {
            AuthzDecision result = AuthzDecision.valueOf((String) response.state().getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT));
            decision = decision.mergeDecision(result);

            if (log.isTraceEnabled()) {
                log.trace("Policy response for " + ctxToAuthorize.hashCode() + ", policy = " + response.path() + ", result = " + result + ", merged = " + decision);
            }

            if (decision == AuthzDecision.REJECT) {
                authorized(false);
            } else {
                next();
            }
        }

        public void next() {
            AuthzPolicyEntry next = queue.poll();
            if (next == null) {
                authorized(decision == AuthzDecision.ACCEPT);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Invoking next policy for " + ctxToAuthorize.hashCode() + ", policy = " + next.getPolicyResourceEndpoint());
                }

                RequestContext policyReq = createPolicyReq();
                client.read(policyReq, next.getPolicyResourceEndpoint(), PolicyHandler.this);
            }
        }

        private RequestContext createPolicyReq() {
            RequestAttributes attribs = new DefaultRequestAttributes();
            attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, ctxToAuthorize);
            return new RequestContext.Builder().requestAttributes(attribs).build();
        }

        private void authorized(boolean accepted) {
            if (log.isTraceEnabled()) {
                log.trace("Completed for " + ctxToAuthorize.hashCode() + ", merged = " + decision);
            }

            sink.accept(AuthzConstants.ATTR_AUTHZ_RESULT, accepted);
            try {
                sink.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Queue<AuthzPolicyEntry> getPolicies(ResourcePath resPath) {
            Queue<AuthzPolicyEntry> l = new LinkedList<>();
            if (policies != null) {
                for (AuthzPolicyEntry policyEntry : policies) {
                    if (policyEntry.isResourceMapped(resPath)) {
                        l.add(policyEntry);
                    }
                }
            }
            return l;
        }

    }

}
