package org.projectodd.restafari.security.policy.uri;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.projectodd.restafari.security.impl.AuthConstants;
import org.projectodd.restafari.security.impl.AuthServicesHolder;
import org.projectodd.restafari.security.impl.AuthTokenImpl;
import org.projectodd.restafari.security.spi.AuthToken;
import org.projectodd.restafari.security.spi.AuthorizationRequestContext;
import org.projectodd.restafari.security.spi.AuthorizationService;
import org.projectodd.restafari.security.impl.JsonWebToken;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.RequestType;
import org.projectodd.restafari.spi.ResourcePath;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationServiceTest {

    @Test
    public void testDemoAuthorizationService() {
        AuthServicesHolder.getInstance().registerDefaultPolicies();

        AuthorizationService service = AuthServicesHolder.getInstance().getAuthorizationService();

        AuthorizationRequestContext req1 = createAuthRequestContext("/something", RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req1));

        AuthorizationRequestContext req2 = createAuthRequestContext("/authTest/public/12345", RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req2));

        AuthorizationRequestContext req3 = createAuthRequestContext("/authTest/protected1/12345", RequestType.READ,
                new String[] {}, new String[] {"powerUser"});
        Assert.assertTrue(service.isAuthorized(req3));

        AuthorizationRequestContext req4 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[] {}, new String[] {"powerUser"});
        Assert.assertFalse(service.isAuthorized(req4));

        AuthorizationRequestContext req5 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[] {}, new String[] {"user", "powerUser"});
        Assert.assertTrue(service.isAuthorized(req5));

        AuthorizationRequestContext req6 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[] {}, new String[] {"user", "powerUser"});
        Assert.assertFalse(service.isAuthorized(req6));

        AuthorizationRequestContext req7 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[] {}, new String[] {"user", "admin"});
        Assert.assertTrue(service.isAuthorized(req7));

        AuthorizationRequestContext req8 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[] {}, new String[] {"user", "admin"});
        Assert.assertFalse(service.isAuthorized(req8));

        AuthorizationRequestContext req9 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[] {"user"}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req9));

        AuthorizationRequestContext req10 = createAuthRequestContext("/authTest/protected2", RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req10));

        AuthorizationRequestContext req11 = createAuthRequestContext("/authTest/public", RequestType.READ,
                new String[] {}, new String[] {});
        Assert.assertTrue(service.isAuthorized(req11));
    }

    private AuthorizationRequestContext createAuthRequestContext(String uri, RequestType reqType, String[] realmRoles, String[] appRoles) {
        ResourcePath resPath = new ResourcePath(uri);
        RequestContext reqContext = new AuthTestRequestContext(reqType, resPath);

        Set<String> realmRolesSet = arrayToSet(realmRoles);
        Map<String, Set<String>> appRolesMap = new HashMap<>();
        appRolesMap.put(AuthConstants.DEFAULT_APPLICATION_NAME, arrayToSet(appRoles));

        AuthToken authToken = new AuthTokenImpl(null, null, null, 0, 0, 0, null, realmRolesSet, appRolesMap);
        return new AuthorizationRequestContext(authToken, reqContext);
    }

    private Set<String> arrayToSet(String[] array) {
        Set<String> set = new HashSet<>();
        for (String item : array) {
            set.add(item);
        }
        return set;
    }
}
