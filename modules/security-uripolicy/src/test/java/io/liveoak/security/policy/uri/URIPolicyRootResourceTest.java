package io.liveoak.security.policy.uri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.security.policy.uri.extension.URIPolicyExtension;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigResource;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyRootResourceTest extends AbstractResourceTestCase {

    private SecurityContext anonymous;
    private SecurityContext user;
    private SecurityContext evil;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "uri-policy", new URIPolicyExtension() );
        installResource( "uri-policy", "uri-policy", getPolicyConfig() );
    }

    private ObjectNode getPolicyConfig() throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        ObjectNode objectNode = om.readValue(getClass().getClassLoader().getResourceAsStream("policy-config/uri-policy-config.json"), ObjectNode.class);
        return objectNode;
    }

    @Before
    public void createSecurityContexts() {
        // create some sample securityContext instances
        this.anonymous = new DefaultSecurityContext();

        DefaultSecurityContext user = new DefaultSecurityContext();
        Set<String> s2 = new HashSet();
        s2.addAll(Arrays.asList(new String[]{"test-app/user"}));
        user.setSubject("john");
        user.setRoles(s2);
        this.user = user;

        DefaultSecurityContext evil = new DefaultSecurityContext();
        evil.setSubject("evil");
        evil.setRoles(s2);
        user.setRoles(s2);
        this.evil = evil;
    }

    @Test
    public void testURIPolicyServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/testApp/uri-policy");
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
        // Request to 'client' page
        RequestContext.Builder clientReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/client/some"));
        assertAuthzDecision(clientReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(clientReq.securityContext(evil), AuthzDecision.ACCEPT);

        // request to /app/some
        RequestContext.Builder appReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/app/some"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(evil), AuthzDecision.IGNORE);

        // request to /app/some
        RequestContext.Builder appIndexReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/app/index.html"));
        assertAuthzDecision(appIndexReq.securityContext(anonymous), AuthzDecision.ACCEPT);
        assertAuthzDecision(appIndexReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(appIndexReq.securityContext(evil), AuthzDecision.ACCEPT);

        // READ request to storage
        RequestContext.Builder storageReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/storage"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.REJECT);

        // READ some collection in storage
        storageReq.resourcePath(new ResourcePath("/testApp/storage/todomvc"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.REJECT);

        // CREATE request to storage
        storageReq.requestType(RequestType.CREATE);
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.IGNORE);
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        // READ request to storage
        RequestContext.Builder storageReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/testApp/storage"));
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.REJECT);

        // Find and remove storage rule
        Map<String, List<String>> params = new HashMap<>();
        params.put("runtime", new ArrayList<>());
        ResourceParams resourceParams = DefaultResourceParams.instance(params);
        RequestContext reqCtx = new RequestContext.Builder().resourceParams(resourceParams).build();
        ResourceState config = client.read(reqCtx, "/admin/applications/testApp/resources/uri-policy");
        List<ResourceState> rules = (List<ResourceState>)config.getProperty(URIPolicyConfigResource.RULES_PROPERTY);

        ResourceState storageRule = null;
        for (ResourceState rule : rules) {
            if (rule.getProperty("uriPattern").equals("/testApp/storage*")) {
                storageRule = rule;
                break;
            }
        }
        Assert.assertNotNull(storageRule);
        rules.remove(storageRule);

        // Update config with removed storage rule
        client.update(reqCtx, "/admin/applications/testApp/resources/uri-policy", config);

        // READ request to storage
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.IGNORE);

        // Remove section about deniedUsers and add it back
        storageRule.removeProperty("deniedUsers");
        rules.add(storageRule);
        client.update(reqCtx, "/admin/applications/testApp/resources/uri-policy", config);

        // READ request to storage now allowed even for 'evil'
        assertAuthzDecision(storageReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(storageReq.securityContext(user), AuthzDecision.ACCEPT);
        assertAuthzDecision(storageReq.securityContext(evil), AuthzDecision.ACCEPT);
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
