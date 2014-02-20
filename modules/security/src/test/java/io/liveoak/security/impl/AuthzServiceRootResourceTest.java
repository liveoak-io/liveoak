/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.SecurityServices;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.security.spi.AuthzPolicyEntry;
import io.liveoak.spi.*;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.liveoak.testtools.MockExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResourceTest extends AbstractResourceTestCase {

    private MockAuthzRootPolicyResource mockPolicy;
    private AuthzServiceRootResource authzService;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("authz", new SecurityExtension());
        loadExtension( "mock-policy", new MockExtension( MockAuthzRootPolicyResource.class ));
        installResource("authz", "authz", JsonNodeFactory.instance.objectNode());
        installResource( "mock-policy", "mock-policy", JsonNodeFactory.instance.objectNode() );
    }

    @Before
    public void before() throws Exception {
        this.authzService = (AuthzServiceRootResource) this.system.service(LiveOak.resource("testApp", "authz"));
        this.mockPolicy = (MockAuthzRootPolicyResource) this.system.service(LiveOak.resource("testApp", "mock-policy" ) );
    }

    @Test
    public void testAuthzServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/testApp/authz");
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
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testInvalidPolicyEndpoint() throws Exception {
        AuthzPolicyEntry policyEntry = new AuthzPolicyEntry();
        policyEntry.setPolicyName("Mock Policy");
        policyEntry.setPolicyResourceEndpoint("/invalid");
        authzService.policies(Collections.singletonList(policyEntry));
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzAccept() throws Exception {
        addPolicy(AuthzDecision.ACCEPT);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzReject() throws Exception {
        addPolicy(AuthzDecision.REJECT);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzIgnore() throws Exception {
        addPolicy(AuthzDecision.IGNORE);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    private void addPolicy(AuthzDecision decision) {
        mockPolicy.setAuthzDecision(decision);
        AuthzPolicyEntry policyEntry = new AuthzPolicyEntry();
        policyEntry.setPolicyName("Mock Policy");
        policyEntry.setPolicyResourceEndpoint("/testApp/" + mockPolicy.id());
        authzService.policies(Collections.singletonList(policyEntry));
    }

    private boolean getAuthzResult(RequestContext reqCtxToCheck) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testApp/authz/authzCheck");
        return (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);
    }

}
