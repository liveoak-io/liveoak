package io.liveoak.security.policy.uri;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import io.liveoak.security.impl.AuthConstants;
import io.liveoak.security.impl.AuthServicesHolder;
import io.liveoak.security.impl.DefaultAuthToken;
import io.liveoak.security.spi.AuthToken;
import io.liveoak.security.spi.AuthorizationRequestContext;
import io.liveoak.security.spi.AuthorizationService;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationServiceTest {

    private static AuthorizationService service;

    @BeforeClass
    public static void initAuthorizationService() {
        AuthServicesHolder.getInstance().registerDefaultPolicies();
        service = AuthServicesHolder.getInstance().getAuthorizationService();
    }

    @Test
    public void testSimpleURIPolicy() {
        // All requests here are processed just with DemoSimpleURIPolicy

        AuthorizationRequestContext req1 = createAuthRequestContext("/something", RequestType.READ,
                new String[] {}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req1));

        AuthorizationRequestContext req2 = createAuthRequestContext("/authTest/public/12345", RequestType.READ,
                new String[] {}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req2));

        AuthorizationRequestContext req3 = createAuthRequestContext("/authTest/protected1/12345", RequestType.READ,
                new String[] {}, new String[] {"powerUser"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req3));

        AuthorizationRequestContext req4 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[] {}, new String[] {"powerUser"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req4));

        AuthorizationRequestContext req5 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[] {}, new String[] {"user", "powerUser"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req5));

        AuthorizationRequestContext req6 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[] {}, new String[] {"user", "powerUser"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req6));

        AuthorizationRequestContext req7 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[] {}, new String[] {"user", "admin"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req7));

        AuthorizationRequestContext req8 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[] {}, new String[] {"user", "admin"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req8));

        AuthorizationRequestContext req9 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[] {"user"}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req9));

        AuthorizationRequestContext req10 = createAuthRequestContext("/authTest/protected2", RequestType.READ,
                new String[] {}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req10));

        AuthorizationRequestContext req11 = createAuthRequestContext("/authTest/public", RequestType.READ,
                new String[] {}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req11));
    }

    @Test
    public void testComplexURIPolicy() {
        // Allowed thanks to DemoURIPolicy.rule1 and DemoSimpleURIPolicy.rule1
        AuthorizationRequestContext req1 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[] {"role1"}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req1));

        // Allowed thanks to DemoSimpleURIPolicy.rule1 (DemoURIPolicy ignores it because there is no role1)
        AuthorizationRequestContext req2 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[] {}, new String[] {}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req2));

        // Rejected thanks to DemoURIPolicy.rule7
        AuthorizationRequestContext req3 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[] {"evilRole"}, new String[] {}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req3));

        // Allowed thanks to DemoSimpleURIPolicy.rule2
        Map<String, List<String>> req4params = new HashMap<>();
        req4params.put("param1", Arrays.asList(new String[]{"foo"}));
        req4params.put("param2", Arrays.asList(new String[] {"10"}));
        AuthorizationRequestContext req4 = createAuthRequestContext("/droolsTest/foo/bar", RequestType.READ, new String[] {"role1"}, new String[] {}, AuthTestResourceParams.instance(req4params));
        Assert.assertTrue(service.isAuthorized(req4));

        // Ignored in DemoURIPolicy.rule2 becaused foo<10 and /droolsTest/foo/bar is not mapped to DemoSimpleURIPolicy
        Map<String, List<String>> req5params = new HashMap<>();
        req5params.put("param1", Arrays.asList(new String[]{"foo"}));
        req5params.put("param2", Arrays.asList(new String[] {"9"}));
        AuthorizationRequestContext req5 = createAuthRequestContext("/droolsTest/foo/bar", RequestType.READ, new String[] {"role1"}, new String[] {}, AuthTestResourceParams.instance(req5params));
        Assert.assertFalse(service.isAuthorized(req5));
    }

    private AuthorizationRequestContext createAuthRequestContext(String uri, RequestType reqType,
                                                                 String[] realmRoles, String[] appRoles, ResourceParams resourceParams) {
        ResourcePath resPath = new ResourcePath(uri);
        RequestContext reqContext = new AuthTestRequestContext(reqType, resPath, resourceParams);

        Set<String> realmRolesSet = arrayToSet(realmRoles);
        Map<String, Set<String>> appRolesMap = new HashMap<>();
        appRolesMap.put(AuthConstants.DEFAULT_APPLICATION_NAME, arrayToSet(appRoles));

        AuthToken authToken = new DefaultAuthToken(null, null, null, 0, 0, 0, null, realmRolesSet, appRolesMap);
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
