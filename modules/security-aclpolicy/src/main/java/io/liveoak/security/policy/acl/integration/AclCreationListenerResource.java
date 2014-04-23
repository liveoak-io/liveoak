/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl.integration;

import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.security.policy.acl.AclPolicyConstants;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclCreationListenerResource implements Resource {

    private final Resource parent;
    private final String id;
    private final AclPolicy policy;

    public AclCreationListenerResource(AclPolicyRootResource parent, String id, AclPolicy policy) {
        this.parent = parent;
        this.id = id;
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
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            if (policy != null) {
                ResourceResponse createdResourceResponse = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AclPolicyConstants.ATTR_CREATED_RESOURCE_RESPONSE, ResourceResponse.class) : null;
                if (createdResourceResponse == null) {
                    responder.invalidRequest("Attribute with resource response of created resource not available");
                } else {
                    ResourceState responseState = policy.autocreateAce(createdResourceResponse);
                    Resource resource = ResourceConversionUtils.convertResourceState(responseState, this);
                    responder.resourceUpdated(resource);
                }
            }
        } catch (Throwable t) {
            responder.internalError(t);
        }
    }
}
