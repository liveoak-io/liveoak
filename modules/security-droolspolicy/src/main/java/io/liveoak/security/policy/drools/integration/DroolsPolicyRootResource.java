/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.integration;

import java.util.Collection;
import java.util.LinkedList;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.security.policy.drools.impl.DroolsPolicy;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyRootResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;

    private final DroolsPolicyCheckResource policyCheckResource;

    public DroolsPolicyRootResource(String id, DroolsPolicy policy) {
        this.id = id;
        this.policyCheckResource = new DroolsPolicyCheckResource(this, AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, policy);
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
    public Resource member(RequestContext ctx, String id) {
        if (id.equals(this.policyCheckResource.id())) {
            return this.policyCheckResource;
        }
        return null;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        LinkedList<Resource> members = new LinkedList<>();
        members.add(this.policyCheckResource);
        return members;
    }

}
