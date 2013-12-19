package io.liveoak.security.policy.acl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyRootResourceTest extends AbstractResourceTestCase {

    @Before
    public void before() throws Exception {
        RootResource mock = new MockResource("mock");
        this.system.directDeployer().deploy(mock, new DefaultResourceState());
    }

    @Override
    public RootResource createRootResource() {
        return new AclPolicyRootResource("aclPolicy");
    }

    @Override
    public ResourceState createConfig() {
        ResourceState state = super.createConfig();
        state.putProperty("policy-config", System.getProperty("user.dir") + "/src/test/resources/policy-config/acl-policy-config.json");
        return state;
    }

    @Test
    public void testAclPolicyServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/aclPolicy");
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

        // READ request to /mock/1 should be IGNORED for all users
        RequestContext.Builder appReq = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // CREATE request to /mock/1 should be IGNORED for all users
        appReq = new RequestContext.Builder().requestType(RequestType.CREATE)
                .resourcePath(new ResourcePath("/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // UPDATE request to /mock/1 should be ALLOWED for john thanks to allowedUserAttribute and for admin thanks to allowedRolesAttribute
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.ACCEPT);

        // DELETE request to /mock/1 should be ALLOWED just for john
        appReq = new RequestContext.Builder().requestType(RequestType.DELETE)
                .resourcePath(new ResourcePath("/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.ACCEPT);

        // UPDATE request to /mock/2 should be IGNORED for all users as property with username can't be obtained
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/mock/2"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // UPDATE request to /mock/notFound should be IGNORED for all users as resource doesn't exist
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/mock/notFound"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/aclPolicy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }

}
