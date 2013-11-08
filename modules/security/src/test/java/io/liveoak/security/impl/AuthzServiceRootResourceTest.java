/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.container.auth.AuthzConstants;
import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.security.spi.AuthzDecision;
import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResourceTest extends AbstractResourceTestCase {

    private MockAuthzRootPolicyResource mockPolicy;
    private AuthzServiceRootResource authzService;

    @Before
    public void before() throws InitializationException {
        mockPolicy = new MockAuthzRootPolicyResource("mock-policy");
        system.container().registerResource(mockPolicy);
    }

    @After
    public void after() {
        authzService.setPolicies(null);
    }

    @Override
    public RootResource createRootResource() {
        authzService = new AuthzServiceRootResource("authz");
        return authzService;
    }

    @Test
    public void testAuthzServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/authz");
        boolean authzCheckFound = false;
        for (ResourceState member : state.members()) {
            if (member.id().equals("authzCheck")) {
                authzCheckFound = true;
                break;
            }
        }
        Assert.assertTrue("Child resource 'authzCheck' not found", authzCheckFound);
    }

    @Test
    public void testAuthzCheckNullRequestContext() throws Exception {
        Assert.assertFalse(getAuthzResult(null));
    }

    @Test
    public void testAuthzCheckNoPolicies() throws Exception {
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzAccept() throws Exception {
        addPolicy(AuthzDecision.ACCEPT);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzReject() throws Exception {
        addPolicy(AuthzDecision.REJECT);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzIgnore() throws Exception {
        addPolicy(AuthzDecision.IGNORE);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    private void addPolicy(AuthzDecision decision) {
        mockPolicy.setAuthzDecision(decision);
        AuthzPolicyEntry policyEntry = new AuthzPolicyEntry();
        policyEntry.setPolicyName("Mock Policy");
        policyEntry.setPolicyResourceEndpoint("/" + mockPolicy.id());
        authzService.setPolicies(Collections.singletonList(policyEntry));
    }

    private boolean getAuthzResult(RequestContext reqCtxToCheck) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/authz/authzCheck");
        return (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);
    }

}
