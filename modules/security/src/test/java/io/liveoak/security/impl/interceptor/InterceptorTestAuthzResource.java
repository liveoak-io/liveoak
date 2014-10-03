/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorTestAuthzResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;
    private AuthzWorker worker;

    public InterceptorTestAuthzResource(String id) {
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return id;
    }

    public void setWorker(AuthzWorker worker) {
        this.worker = worker;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        AuthzDecision decision = worker == null ? AuthzDecision.IGNORE : worker.isAuthorized(ctx);
        result.put(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        return result;
    }

    public interface AuthzWorker {

        AuthzDecision isAuthorized(RequestContext ctx);
    }
}
