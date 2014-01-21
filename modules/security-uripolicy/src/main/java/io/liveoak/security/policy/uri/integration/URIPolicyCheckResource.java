/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.integration;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyCheckResource implements Resource {

    private static final Logger log = Logger.getLogger(URIPolicyCheckResource.class);

    private final String id;
    private final URIPolicyRootResource parent;
    private final URIPolicy policy;

    public URIPolicyCheckResource(URIPolicyRootResource parent, String id, URIPolicy policy) {
        this.id = id;
        this.parent = parent;
        this.policy = policy;
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
        AuthzDecision decision = null;

        try {
            if (policy != null) {
                RequestContext reqCtxToAuthorize = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class) : null;
                ResourceState reqResourceState = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, ResourceState.class) : null;
                if (reqCtxToAuthorize == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Request is null. Rejecting");
                    }
                    decision = AuthzDecision.REJECT;
                } else {
                    decision = policy.isAuthorized(reqCtxToAuthorize, reqResourceState);
                }
            }
        } catch (Throwable t) {
            log.error("Error during authz check", t);
            decision = AuthzDecision.REJECT;
        }

        if (decision == null) {
            decision = AuthzDecision.IGNORE;
        }

        sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        sink.close();
    }
}
