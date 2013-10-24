package org.projectodd.restafari.container.auth;

import org.junit.Assert;
import org.junit.Test;
import org.projectodd.restafari.container.ResourceParams;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.auth.service.AuthorizationRequestContext;
import org.projectodd.restafari.container.auth.service.DemoAuthorizationService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationServiceTest {

    @Test
    public void testDemoAuthorizationService() {
        DemoAuthorizationService service = new DemoAuthorizationService();

        AuthorizationRequestContext req1 = createAuthRequestContext("/something", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req1));

        AuthorizationRequestContext req2 = createAuthRequestContext("/authTest/public/12345", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req2));

        AuthorizationRequestContext req3 = createAuthRequestContext("/authTest/protected1/12345", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {"powerUsers"});
        Assert.assertTrue(service.isAuthorized(req3));

        AuthorizationRequestContext req4 = createAuthRequestContext("/authTest/protected1/6789", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {"powerUsers"});
        Assert.assertFalse(service.isAuthorized(req4));

        AuthorizationRequestContext req5 = createAuthRequestContext("/authTest/protected1/6789", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {"users", "powerUsers"});
        Assert.assertTrue(service.isAuthorized(req5));

        AuthorizationRequestContext req6 = createAuthRequestContext("/authTest/protected1/6789", ResourceRequest.RequestType.CREATE,
                new String[] {}, new String[] {"users", "powerUsers"});
        Assert.assertFalse(service.isAuthorized(req6));

        AuthorizationRequestContext req7 = createAuthRequestContext("/authTest/protected1/6789", ResourceRequest.RequestType.CREATE,
                new String[] {}, new String[] {"users", "admins"});
        Assert.assertTrue(service.isAuthorized(req7));

        AuthorizationRequestContext req8 = createAuthRequestContext("/authTest/protected2", ResourceRequest.RequestType.CREATE,
                new String[] {}, new String[] {"users", "admins"});
        Assert.assertFalse(service.isAuthorized(req8));

        AuthorizationRequestContext req9 = createAuthRequestContext("/authTest/protected2", ResourceRequest.RequestType.CREATE,
                new String[] {"users"}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req9));

        AuthorizationRequestContext req10 = createAuthRequestContext("/authTest/protected2", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req10));

        AuthorizationRequestContext req11 = createAuthRequestContext("/authTest/public", ResourceRequest.RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req11));
    }

    private AuthorizationRequestContext createAuthRequestContext(String uri, ResourceRequest.RequestType reqType, String[] realmRoles, String[] appRoles) {
        ResourcePath resPath = new ResourcePath(uri);
        ResourceRequest req = new ResourceRequest.Builder(reqType, resPath).build();

        Map<String, String[]> appRolesMap = new HashMap<>();
        appRolesMap.put("authTest", appRoles);
        JsonWebToken accessToken = new JsonWebToken(realmRoles, appRolesMap);

        return new AuthorizationRequestContext(accessToken, req);
    }
}
