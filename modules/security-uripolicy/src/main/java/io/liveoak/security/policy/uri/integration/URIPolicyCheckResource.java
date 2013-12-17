/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.integration;

import io.liveoak.container.auth.AuthzConstants;
import io.liveoak.container.auth.SimpleLogger;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.security.spi.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyCheckResource implements Resource {

    private static final SimpleLogger log = new SimpleLogger(URIPolicyCheckResource.class);

    private final String id;
    private final URIPolicyRootResource parent;

    public URIPolicyCheckResource(String id, URIPolicyRootResource parent) {
        this.id = id;
        this.parent = parent;
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
            URIPolicy uriPolicy = parent.getUriPolicy();
            if (uriPolicy != null) {
                RequestContext reqCtxToAuthorize = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, RequestContext.class) : null;
                if (reqCtxToAuthorize == null) {
                    throw new NullPointerException();
                }
                decision = uriPolicy.isAuthorized(reqCtxToAuthorize);
            }
        } catch (Throwable t) {
            decision = AuthzDecision.REJECT;
        }

        if (decision == null) {
            decision = AuthzDecision.IGNORE;
        }

        sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        sink.close();
    }
}
