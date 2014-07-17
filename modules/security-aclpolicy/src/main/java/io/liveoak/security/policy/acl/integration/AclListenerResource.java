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
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclListenerResource implements Resource {

    private final Resource parent;
    private final String id;
    private final AclPolicy policy;

    public AclListenerResource(AclPolicyRootResource parent, String id, AclPolicy policy) {
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
                ResourceResponse resourceResponse = ctx.requestAttributes() != null ? ctx.requestAttributes().getAttribute(AclPolicyConstants.ATTR_CREATED_RESOURCE_RESPONSE, ResourceResponse.class) : null;
                ResourceState result = null;

                if (resourceResponse == null) {
                    responder.invalidRequest("Attribute with resource response of created resource not available");
                    return;
                } else if (resourceResponse.responseType() == ResourceResponse.ResponseType.CREATED) {
                    result = policy.autocreateAce(resourceResponse);
                } else if (resourceResponse.responseType() == ResourceResponse.ResponseType.DELETED) {
                    result = policy.deleteAce(resourceResponse);
                }

                Resource resource = ResourceConversionUtils.convertResourceState(result, this);
                responder.resourceUpdated(resource);
            }
        } catch (Throwable t) {
            responder.internalError(t);
        }
    }
}
