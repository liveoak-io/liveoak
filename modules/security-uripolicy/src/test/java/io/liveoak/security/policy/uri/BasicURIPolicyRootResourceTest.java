/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.uri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.security.policy.uri.extension.URIPolicyExtension;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BasicURIPolicyRootResourceTest extends AbstractTestCaseWithTestApp {

    private SecurityContext anonymous;
    private SecurityContext mary;
    private SecurityContext john;
    private SecurityContext bob;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("uri-policy", new URIPolicyExtension());
        installTestAppResource("uri-policy", "uri-policy", getPolicyConfig());
    }

    private static ObjectNode getPolicyConfig() throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        ObjectNode objectNode = om.readValue(BasicURIPolicyRootResourceTest.class.getClassLoader().getResourceAsStream("policy-config/basic-uri-policy-config.json"), ObjectNode.class);
        return objectNode;
    }

    @Before
    public void createSecurityContexts() {
        // create some sample securityContext instances
        this.anonymous = new DefaultSecurityContext();

        DefaultSecurityContext mary = new DefaultSecurityContext();
        mary.setSubject("mary");
        this.mary = mary;

        DefaultSecurityContext john = new DefaultSecurityContext();
        Set<String> s1 = new HashSet();
        s1.addAll(Arrays.asList(new String[]{"test-app/user"}));
        john.setSubject("john");
        john.setRoles(s1);
        this.john = john;

        DefaultSecurityContext bob = new DefaultSecurityContext();
        Set<String> s2 = new HashSet();
        s2.addAll(Arrays.asList(new String[]{"test-app/user", "test-app/admin"}));
        bob.setSubject("bob");
        bob.setRoles(s2);
        this.bob = bob;
    }

    @Test
    public void testAuthorizationRequest() throws Exception {
        // Request to /client/liveoak.js
        RequestContext.Builder clientReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/client/liveoak.js"));
        assertAuthzDecision(clientReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(mary), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(mary), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(bob), AuthzDecision.ACCEPT);

        // request to /app/index.html
        RequestContext.Builder appReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/app/index.html"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(mary), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(john), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(bob), AuthzDecision.ACCEPT);

        // READ request to /storage/unsecured-col
        RequestContext.Builder unsecuredColReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/storage/unsecured-col"));
        assertAuthzDecision(unsecuredColReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(unsecuredColReq.securityContext(mary), AuthzDecision.ACCEPT);
        assertAuthzDecision(unsecuredColReq.securityContext(john), AuthzDecision.ACCEPT);
        assertAuthzDecision(unsecuredColReq.securityContext(bob), AuthzDecision.ACCEPT);

        // READ request to /storage/todos
        RequestContext.Builder storageReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/storage/todos"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(mary), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(john), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(bob), AuthzDecision.ACCEPT);

        // CREATE request to /storage/todos
        storageReq.requestType(RequestType.CREATE).resourcePath(new ResourcePath("/testApp/storage/todos"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(mary), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(john), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(bob), AuthzDecision.ACCEPT);

        // READ request to /storage/todos/something
        storageReq.requestType(RequestType.READ).resourcePath(new ResourcePath("/testApp/storage/todos/something"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(mary), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(john), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(bob), AuthzDecision.ACCEPT);

        // UPDATE request to /storage/todos/something
        storageReq.requestType(RequestType.UPDATE).resourcePath(new ResourcePath("/testApp/storage/todos/something"));
        ;
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(mary), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(john), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(bob), AuthzDecision.ACCEPT);
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        assertAuthzDecision(reqCtxToCheck, null, expectedDecision);
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, ResourceState reqResourceState, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, reqResourceState);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testApp/uri-policy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }

}
