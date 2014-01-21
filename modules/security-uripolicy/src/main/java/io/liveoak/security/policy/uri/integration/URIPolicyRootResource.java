/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.integration;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyRootResource implements RootResource {

    private Resource parent;
    private String id;

    private final URIPolicyCheckResource policyCheckResource;

    public URIPolicyRootResource(String id, URIPolicy policy) {
        this.id = id;
        this.policyCheckResource = new URIPolicyCheckResource(this, AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, policy);
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

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        if (id.equals(this.policyCheckResource.id())) {
            responder.resourceRead(this.policyCheckResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.accept(this.policyCheckResource);
        sink.close();
    }

}
