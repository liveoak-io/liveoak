/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorTestAuthzResource implements RootResource {

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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        try {
            AuthzDecision decision = worker == null ? AuthzDecision.IGNORE : worker.isAuthorized(ctx);
            sink.accept(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT, decision.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        sink.close();
    }

    public interface AuthzWorker {

        AuthzDecision isAuthorized(RequestContext ctx);
    }
}
