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

    private String id;
    private URIPolicy uriPolicy;

    private final Map<String, Resource> childResources = new HashMap<>();

    private URIPolicyConfigResource config = new URIPolicyConfigResource(this);

    public URIPolicyRootResource(String id) {
        this.id = id;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        registerChildrenResources();
    }

    protected void registerChildrenResources() {
        this.childResources.put(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, new URIPolicyCheckResource(AuthzConstants.AUTHZ_CHECK_RESOURCE_ID, this));
    }

    @Override
    public void destroy() {
        // Nothing here for now
    }

    @Override
    public String id() {
        return id;
    }

    public URIPolicy getUriPolicy() {
        return uriPolicy;
    }

    public void setUriPolicy(URIPolicy uriPolicy) {
        this.uriPolicy = uriPolicy;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        try {
            if (!this.childResources.containsKey(id)) {
                responder.noSuchResource(id);
                return;
            }

            responder.resourceRead(this.childResources.get(id));

        } catch (Throwable t) {
            responder.internalError(t.getMessage());
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.childResources.values().forEach((e) -> {
            sink.accept(e);
        });

        sink.close();
    }

    @Override
    public Resource configuration() {
        return config;
    }

}
