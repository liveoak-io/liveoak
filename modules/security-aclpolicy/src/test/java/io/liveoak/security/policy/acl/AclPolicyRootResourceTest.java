package io.liveoak.security.policy.acl;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.policy.acl.extension.SecurityACLPolicyExtension;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.liveoak.testtools.MockExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyRootResourceTest extends AbstractResourceTestCase {

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "aclPolicy", new SecurityACLPolicyExtension() );
        loadExtension( "mock", new MockExtension( MockResource.class ) );
    }

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "src/test/resources/policy-config" );
    }

    @Test
    public void testAclPolicyServiceRequest() throws Exception {
        RequestContext reqCtx = new RequestContext.Builder().build();
        ResourceState state = client.read(reqCtx, "/testOrg/testApp/aclPolicy");
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
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // CREATE request to /mock/1 should be IGNORED for all users
        appReq = new RequestContext.Builder().requestType(RequestType.CREATE)
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // UPDATE request to /mock/1 should be ALLOWED for john thanks to allowedUserAttribute and for admin thanks to allowedRolesAttribute
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.ACCEPT);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.ACCEPT);

        // DELETE request to /mock/1 should be ALLOWED just for john
        appReq = new RequestContext.Builder().requestType(RequestType.DELETE)
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/1"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.ACCEPT);

        // UPDATE request to /mock/2 should be IGNORED for all users as property with username can't be obtained
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/2"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);

        // UPDATE request to /mock/notFound should be IGNORED for all users as resource doesn't exist
        appReq = new RequestContext.Builder().requestType(RequestType.UPDATE)
                .resourcePath(new ResourcePath("/testOrg/testApp/mock/notFound"));
        assertAuthzDecision(appReq.securityContext(anonymous), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(admin), AuthzDecision.IGNORE);
        assertAuthzDecision(appReq.securityContext(user), AuthzDecision.IGNORE);
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testOrg/testApp/aclPolicy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }

}
