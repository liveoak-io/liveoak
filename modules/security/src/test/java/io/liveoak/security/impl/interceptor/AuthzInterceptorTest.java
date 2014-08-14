/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.security.extension.SecurityExtension;
import io.liveoak.security.integration.AuthzServiceConfigResource;
import io.liveoak.security.integration.AuthzServiceRootResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.NotAuthorizedException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.liveoak.testtools.MockExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthzInterceptorTest extends AbstractResourceTestCase {

    private InterceptorTestAuthzResource mockPolicy;
    private MockAuthInterceptor mockAuthInterceptor = new MockAuthInterceptor();

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("interceptor", new InterceptorExtension(), getInterceptorConfig());
        loadExtension("authz", new SecurityExtension());
        loadExtension("mock-resource", new MockExtension( MockInMemoryRootResource.class ));
        loadExtension("mock-policy", new MockExtension( InterceptorTestAuthzResource.class ));
        loadExtension("mock-auth-interceptor", new InterceptorTestExtension(mockAuthInterceptor), JsonNodeFactory.instance.objectNode());

        installResource("authz", "authz", getSecurityConfig());
        installResource("mock-resource", "mock-resource", JsonNodeFactory.instance.objectNode() );
        installResource("mock-policy", "mock-policy", JsonNodeFactory.instance.objectNode() );
    }

    private ObjectNode getInterceptorConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode mockAuth = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "mock-auth")
                .put("resource-path-prefix", "/testApp/mock-resource");
        ObjectNode authz = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "authz")
                .put("resource-path-prefix", "/testApp/mock-resource");
        config.putArray("local").add(mockAuth).add(authz);
        return config;
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
        AuthzServiceRootResource authzRootResource = (AuthzServiceRootResource) this.system.service(LiveOak.resource("testApp", "authz"));
        this.mockPolicy = (InterceptorTestAuthzResource) this.system.service(LiveOak.resource("testApp", "mock-policy" ) );
    }

    @Test
    public void testAuthzInbound() throws Exception {
        mockPolicy.setWorker(new InterceptorTestAuthzResource.AuthzWorker() {

            @Override
            public AuthzDecision isAuthorized(RequestContext ctx) {
                RequestContext ctxToAuthorize = (RequestContext) ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT);
                if (ctxToAuthorize == null || !ctxToAuthorize.securityContext().isAuthenticated()) {
                    return AuthzDecision.REJECT;
                } else {
                    return AuthzDecision.ACCEPT;
                }
            }

        });

        mockAuthInterceptor.setSubject("john");
        RequestContext.Builder reqContext = new RequestContext.Builder()
                .requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/mock-resource"))
                .securityContext(new DefaultSecurityContext());
        ResourceState fullState = client.read(reqContext.returnFields(new DefaultReturnFields("*(*(*))")), "/testApp/mock-resource");
        Assert.assertEquals(3, fullState.members().size());
        ResourceState todos = fullState.members().get(0);
        Assert.assertEquals("todos", todos.id());
        Assert.assertEquals("someValue", todos.getProperty("somePropertyOfTodosCollection"));
        Assert.assertEquals(3, todos.members().size());
        ResourceState todo3 = todos.members().get(2);
        Assert.assertEquals("todo3", todo3.id());
        Assert.assertEquals(0, todo3.members().size());
        Assert.assertEquals("secret todo", todo3.getProperty("title"));
        Assert.assertEquals("bob", todo3.getProperty("user"));

        mockAuthInterceptor.setSubject(null);
        try {
            client.read(reqContext, "/testApp/mock-resource");
            Assert.fail("Not expected to reach this");
        } catch (NotAuthorizedException e) {
        };
    }


    @Test
    public void testAuthzOutbound() throws Exception {
        mockPolicy.setWorker(new InterceptorTestAuthzResource.AuthzWorker() {

            @Override
            public AuthzDecision isAuthorized(RequestContext ctx) {
                ResourceState responseState = (ResourceState) ctx.requestAttributes().getAttribute(AuthzConstants.ATTR_RESPONSE_RESOURCE_STATE);
                if (responseState == null) {
                    return AuthzDecision.ACCEPT;
                } else {
                    String title = (String) responseState.getProperty("title");
                    if (title != null && title.startsWith("secret")) {
                        return AuthzDecision.REJECT;
                    } else {
                        return AuthzDecision.ACCEPT;
                    }
                }
            }

        });

        mockAuthInterceptor.setSubject("john");
        RequestContext.Builder reqContext = new RequestContext.Builder()
                .requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/mock-resource"))
                .securityContext(new DefaultSecurityContext());
        ResourceState state1 = client.read(reqContext.returnFields(new DefaultReturnFields("*")), "/testApp/mock-resource");
        ResourceState state2 = client.read(reqContext.returnFields(new DefaultReturnFields("*(*)")), "/testApp/mock-resource");
        ResourceState state3 = client.read(reqContext.returnFields(new DefaultReturnFields("*(*(*))")), "/testApp/mock-resource");

        Assert.assertEquals(3, state1.members().size());
        ResourceState todos = state1.members().get(0);
        Assert.assertEquals("todos", todos.id());
        Assert.assertEquals("chat", state1.members().get(1).id());
        Assert.assertEquals("secured", state1.members().get(2).id());
        // No any properties or members as it's not expanded
        Assert.assertEquals(0, todos.getPropertyNames().size());
        Assert.assertEquals(0, todos.members().size());

        Assert.assertEquals(2, state2.members().size());
        todos = state2.members().get(0);
        Assert.assertEquals("todos", todos.id());
        Assert.assertEquals(3, todos.members().size());
        Assert.assertEquals("todo1", todos.members().get(0).id());
        Assert.assertEquals("todo2", todos.members().get(1).id());
        Assert.assertEquals("todo3", todos.members().get(2).id());
        // No any properties as it's not expanded
        Assert.assertEquals(0, todos.members().get(0).getPropertyNames().size());

        Assert.assertEquals(2, state3.members().size());
        todos = state3.members().get(0);
        Assert.assertEquals("todos", todos.id());
        Assert.assertEquals(2, todos.members().size());
        Assert.assertEquals("todo1", todos.members().get(0).id());
        Assert.assertEquals("todo2", todos.members().get(1).id());
        // Expanded, so properties of todo are here
        Assert.assertEquals(2, todos.members().get(0).getPropertyNames().size());
    }
}
