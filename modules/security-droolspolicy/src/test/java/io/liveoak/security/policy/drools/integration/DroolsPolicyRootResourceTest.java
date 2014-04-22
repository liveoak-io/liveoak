/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.drools.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.security.policy.drools.extension.DroolsPolicyExtension;
import io.liveoak.spi.*;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DroolsPolicyRootResourceTest extends AbstractResourceTestCase {

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "drools-policy", new DroolsPolicyExtension() );
        installResource( "drools-policy", "drools-policy", getPolicyConfig() );
    }

    private ObjectNode getPolicyConfig() throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        ObjectNode objectNode = om.readValue(getClass().getClassLoader().getResourceAsStream("policy-config/drools-policy-config.json"), ObjectNode.class);
        return objectNode;
    }

    @Test
    public void testDroolsPolicyServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/testApp/drools-policy");
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

        // Request to 'client' page
        RequestContext.Builder clientReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/client/some"));
        assertAuthzDecision(clientReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(clientReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(user), AuthzDecision.IGNORE);

        // Other requests to 'client' page by john.
        assertAuthzDecision(clientReq.resourcePath(new ResourcePath("/testApp/client/somejs")), AuthzDecision.IGNORE);
        assertAuthzDecision(clientReq.resourcePath(new ResourcePath("/testApp/client/some.jsk")), AuthzDecision.IGNORE);
        assertAuthzDecision(clientReq.resourcePath(new ResourcePath("/testApp/client/some.js")), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.resourcePath(new ResourcePath("/testApp/client/auth/some.js")), AuthzDecision.ACCEPT);

        // request to /app/some
        RequestContext.Builder appReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/app/some"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // Other requests to 'app' page by john.
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/somehtml")), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.htmll")), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.html")), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.css")), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.js")), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.png")), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.resourcePath(new ResourcePath("/testApp/app/some.json")), AuthzDecision.IGNORE);

        // CREATE request to app should be ACCEPT just for admin
        appReq.requestType(RequestType.CREATE);
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // Request to /storage/some should be IGNORED for anonymous user, but allowed for user or admin in case that query contains username
        RequestContext.Builder storageReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/storage/some"));
        Map<String, List<String>> params = new HashMap<>();
        params.put("q", Arrays.asList("{\"completed\":\"false\",\"user\":\"john\"}"));
        storageReq.resourceParams(DefaultResourceParams.instance(params));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);

        // IGNORED for user if token from query is different from username
        params.put("q", Arrays.asList("{\"completed\":\"false\",\"user\":\"otherUser\"}"));
        storageReq.resourceParams(DefaultResourceParams.instance(params));
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.IGNORE);

        // CREATE request is REJECT for anonymous and allowed for admin. It's allowed for user if "user" from createState equals username
        storageReq = new RequestContext.Builder().requestType(RequestType.CREATE).resourcePath(new ResourcePath("/testApp/storage/some"));
        ResourceState createState = new DefaultResourceState();
        createState.putProperty("user", "john");
        createState.putProperty("something", "something-which-does-not-matter");
        assertAuthzDecision(storageReq.securityContext(anonymous), createState, AuthzDecision.REJECT);
        assertAuthzDecision(storageReq.securityContext(admin), createState, AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), createState, AuthzDecision.ACCEPT);

        // CREATE not allowed for user without createState
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.REJECT);

        // CREATE request not allowed for user if "user" from createState doesn't equal username
        createState.putProperty("user", "otherUser");
        assertAuthzDecision(storageReq.securityContext(admin), createState, AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), createState, AuthzDecision.REJECT);

        // UPDATE to storage also REJECTS if user from createState is different from current user
        createState.putProperty("user", "john");
        storageReq = new RequestContext.Builder().requestType(RequestType.UPDATE).resourcePath(new ResourcePath("/testApp/storage/some/123"));
        assertAuthzDecision(storageReq.securityContext(anonymous), createState, AuthzDecision.REJECT);
        assertAuthzDecision(storageReq.securityContext(admin), createState, AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), createState, AuthzDecision.IGNORE);
        createState.putProperty("user", "otherUser");
        assertAuthzDecision(storageReq.securityContext(admin), createState, AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(user), createState, AuthzDecision.REJECT);

    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        assertAuthzDecision(reqCtxToCheck, null, expectedDecision);
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, ResourceState reqResourceState, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, reqResourceState);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testApp/drools-policy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }

}
