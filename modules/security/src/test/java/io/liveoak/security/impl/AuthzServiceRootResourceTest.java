/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.security.integration.AuthzServiceConfigResource;
import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.spi.*;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.liveoak.testtools.MockExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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
        installResource("authz", "authz", getSecurityConfig());
        installResource( "mock-policy", "mock-policy", JsonNodeFactory.instance.objectNode() );
    }

    private ObjectNode getSecurityConfig() throws Exception {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode policyConfig = JsonNodeFactory.instance.objectNode();
        policyConfig.put("policyName", "Mock Policy");
        policyConfig.put("policyResourceEndpoint", "/testApp/mock-policy");
        config.putArray(AuthzServiceConfigResource.POLICIES_PROPERTY).add(policyConfig);
        return config;
    }

    @Before
    public void before() throws Exception {
        this.authzService = (AuthzServiceRootResource) this.system.service(Services.resource("testApp", "authz"));
        this.mockPolicy = (MockAuthzRootPolicyResource) this.system.service(Services.resource("testApp", "mock-policy") );
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
        // Retrieve config  and then remove all policies
        RequestContext reqCtx = new RequestContext.Builder();
        ResourceState config = client.read(reqCtx, "/admin/applications/testApp/resources/authz");
        List<ResourceState> rules = (List<ResourceState>)config.getProperty(AuthzServiceConfigResource.POLICIES_PROPERTY);
        rules.clear();

        // Update config with removed policies
        client.update(reqCtx, "/admin/applications/testApp/resources/authz", config);

        // Check that request is permitted if we don't have any policies
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testInvalidPolicyEndpoint() throws Exception {
        // Retrieve config  and then update policy with invalid endpoint
        RequestContext reqCtx = new RequestContext.Builder();
        ResourceState config = client.read(reqCtx, "/admin/applications/testApp/resources/authz");
        List<ResourceState> rules = (List<ResourceState>)config.getProperty(AuthzServiceConfigResource.POLICIES_PROPERTY);
        ResourceState policyConfig = rules.get(0);
        policyConfig.putProperty("policyResourceEndpoint", "/testApp/invalid");

        // Update config with removed policies
        client.update(reqCtx, "/admin/applications/testApp/resources/authz", config);

        // Invalid endpoint, so reject
        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzAccept() throws Exception {
        setMockDecision(AuthzDecision.ACCEPT);

        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.setRealm("liveoak-apps");

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzInvalidRealm() throws Exception {
        setMockDecision(AuthzDecision.ACCEPT);

        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.setRealm("invalid");

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzAdminOverrides() throws Exception {
        setMockDecision(AuthzDecision.REJECT);

        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.setRealm("liveoak-admin");
        securityContext.setRoles(Collections.singleton("admin"));

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some")).securityContext(securityContext);
        Assert.assertTrue(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzReject() throws Exception {
        setMockDecision(AuthzDecision.REJECT);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
    }

    @Test
    public void testAuthzIgnore() throws Exception {
        setMockDecision(AuthzDecision.IGNORE);

        RequestContext reqCtxToCheck = new RequestContext.Builder().requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/some"));
        Assert.assertFalse(getAuthzResult(reqCtxToCheck));
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
