/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.container.auth.AuthzConstants;
import io.liveoak.security.spi.AuthzDecision;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyRootResourceTest extends AbstractResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new URIPolicyRootResource("uriPolicy");
    }

    @Override
    public ResourceState createConfig() {
        ResourceState state = super.createConfig();
        state.putProperty("policy-config", System.getProperty("user.dir") + "/src/test/resources/policy-config/uri-policy-config.json");
        return state;
    }

    @Test
    public void testURIPolicyServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/uriPolicy");
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
        assertAuthzDecision(null, AuthzDecision.REJECT);
    }

    @Test
    public void testAuthorizationRequest() throws Exception {
        // create some sample securityContext instances
        SecurityContext anonymous = new DefaultSecurityContext();
        DefaultSecurityContext admin = new DefaultSecurityContext();
        Set<String> s1 = new HashSet();
        s1.addAll(Arrays.asList(new String[]{"test-app/admin", "test-app/user"}));
        admin.setRealm("default");
        admin.setSubject("admin");
        admin.setRoles(s1);

        DefaultSecurityContext user = new DefaultSecurityContext();
        Set<String> s2 = new HashSet();
        s2.addAll(Arrays.asList(new String[]{"test-app/user"}));
        user.setRealm("default");
        user.setSubject("john");
        user.setRoles(s2);

        // request to /storage/some should be IGNORED for anonymous user, but allowed for user or admin
        RequestContext.Builder storageReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/storage/some"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);

        // request to /app/some should be IGNORED for anonymous user, but allowed for user or admin
        RequestContext.Builder appReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/app/some"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.ACCEPT);

        // CREATE request to app should be ACCEPT just for admin
        appReq.requestType(RequestType.CREATE);
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // Request to public page allowed for everyone
        RequestContext.Builder publicReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/public/some"));
        assertAuthzDecision(publicReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(publicReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(publicReq.securityContext(user), AuthzDecision.ACCEPT);

    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/uriPolicy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }
}
