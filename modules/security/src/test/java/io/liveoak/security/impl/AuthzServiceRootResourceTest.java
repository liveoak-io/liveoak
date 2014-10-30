/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.impl;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.security.integration.AuthzServiceConfigResource;
import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.Services;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import io.liveoak.testtools.MockExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzServiceRootResourceTest extends AbstractTestCaseWithTestApp {

    private MockAuthzRootPolicyResource mockPolicy;
    private AuthzServiceRootResource authzService;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("authz", new SecurityExtension());
        loadExtension("mock-policy", new MockExtension(MockAuthzRootPolicyResource.class));
        installTestAppResource("authz", "authz", getSecurityConfig());
        installTestAppResource("mock-policy", "mock-policy", JsonNodeFactory.instance.objectNode());
    }

    private static ObjectNode getSecurityConfig() throws Exception {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode policyConfig = JsonNodeFactory.instance.objectNode();
        policyConfig.put("policyName", "Mock Policy");
        policyConfig.put("policyResourceEndpoint", "/testApp/mock-policy");
        config.putArray(AuthzServiceConfigResource.POLICIES_PROPERTY).add(policyConfig);
        return config;
    }

    @Before
    public void before() throws Exception {
        this.authzService = (AuthzServiceRootResource) system.service(Services.resource("testApp", "authz"));
        this.mockPolicy = (MockAuthzRootPolicyResource) system.service(Services.resource("testApp", "mock-policy"));
    }

    @Test
    public void authzServiceRootResourceTests() throws Exception {
        // Test #1 - Authz service request
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


        // Test #2 - Authz check null request context
        Assert.assertFalse(getAuthzResult(null));


        // Test #3 - Invalid policy endpoint
        // Retrieve config  and then update policy with invalid endpoint
        reqCtx = new RequestContext.Builder();
        ResourceState config = client.read(reqCtx, "/admin/applications/testApp/resources/authz");
        List<ResourceState> rules = (List<ResourceState>) config.getProperty(AuthzServiceConfigResource.POLICIES_PROPERTY);
        ResourceState policyConfig = rules.get(0);
        policyConfig.putProperty("policyResourceEndpoint", "/testApp/invalid");

        // Update config with removed policies
        client.update(reqCtx, "/admin/applications/testApp/resources/authz", config);

        // Invalid endpoint, so reject
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));


        // Test #4 - Authz reject
        setMockDecision(AuthzDecision.REJECT);

        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));


        // Test #5 - Authz ignore
        setMockDecision(AuthzDecision.IGNORE);

        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));


        // Test #6 - Authz check no policies
        // Retrieve config  and then remove all policies
        reqCtx = new RequestContext.Builder();
        config = client.read(reqCtx, "/admin/applications/testApp/resources/authz");
        rules = (List<ResourceState>) config.getProperty(AuthzServiceConfigResource.POLICIES_PROPERTY);
        rules.clear();

        // Update config with removed policies
        client.update(reqCtx, "/admin/applications/testApp/resources/authz", config);

        // Check that request is permitted if we don't have any policies
        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));


        // Test #7 - Authz accept
        setMockDecision(AuthzDecision.ACCEPT);

        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.setRealm("liveoak-apps");

        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));


        // Test #8 - Authz invalid realm
        setMockDecision(AuthzDecision.ACCEPT);

        securityContext = new DefaultSecurityContext();
        securityContext.setRealm("invalid");

        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));


        // Test #9 - Authz admin overrides
        setMockDecision(AuthzDecision.REJECT);

        securityContext = new DefaultSecurityContext();
        securityContext.setRealm("liveoak-admin");
        securityContext.setRoles(Collections.singleton("admin"));

        reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    private void setMockDecision(AuthzDecision decision) {
        mockPolicy.setAuthzDecision(decision);
    }

    private boolean getAuthzResult(RequestContext reqCtxToCheck) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testApp/authz/authzCheck");
        return (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);
    }

}
