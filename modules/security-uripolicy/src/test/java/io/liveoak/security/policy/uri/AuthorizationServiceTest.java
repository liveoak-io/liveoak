/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.common.security.AuthzConstants;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * TODO: Should be tested with integration tests
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
// TODO Fix
@Ignore
public class AuthorizationServiceTest {
    /*

    private static AuthzService service;

    @BeforeClass
    public static void initAuthorizationService() {
        AuthServicesHolder.getInstance().registerDefaultPolicies();
        service = AuthServicesHolder.getInstance().getAuthzService();
    }

    @Test
    public void testSimpleURIPolicy() {
        // All requests here are processed just with DemoSimpleURIPolicy

        AuthzRequestContext req1 = createAuthRequestContext("/something", RequestType.READ,
                new String[]{}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req1));

        AuthzRequestContext req2 = createAuthRequestContext("/authTest/public/12345", RequestType.READ,
                new String[]{}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req2));

        AuthzRequestContext req3 = createAuthRequestContext("/authTest/protected1/12345", RequestType.READ,
                new String[]{}, new String[]{"powerUser"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req3));

        AuthzRequestContext req4 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[]{}, new String[]{"powerUser"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req4));

        AuthzRequestContext req5 = createAuthRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[]{}, new String[]{"user", "powerUser"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req5));

        AuthzRequestContext req6 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[]{}, new String[]{"user", "powerUser"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req6));

        AuthzRequestContext req7 = createAuthRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[]{}, new String[]{"user", "admin"}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req7));

        AuthzRequestContext req8 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[]{}, new String[]{"user", "admin"}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req8));

        AuthzRequestContext req9 = createAuthRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[]{"user"}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req9));

        AuthzRequestContext req10 = createAuthRequestContext("/authTest/protected2", RequestType.READ,
                new String[]{}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req10));

        AuthzRequestContext req11 = createAuthRequestContext("/authTest/public", RequestType.READ,
                new String[]{}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req11));
    }

    @Test
    public void testComplexURIPolicy() {
        // Allowed thanks to DemoURIPolicyWorker.rule1 and DemoSimpleURIPolicy.rule1
        AuthzRequestContext req1 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[]{"role1"}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req1));

        // Allowed thanks to DemoSimpleURIPolicy.rule1 (DemoURIPolicyWorker ignores it because there is no role1)
        AuthzRequestContext req2 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[]{}, new String[]{}, ResourceParams.NONE);
        Assert.assertTrue(service.isAuthorized(req2));

        // Rejected thanks to DemoURIPolicyWorker.rule7
        AuthzRequestContext req3 = createAuthRequestContext("/droolsTest/foo", RequestType.READ, new String[]{"evilRole"}, new String[]{}, ResourceParams.NONE);
        Assert.assertFalse(service.isAuthorized(req3));

        // Allowed thanks to DemoSimpleURIPolicy.rule2
        Map<String, List<String>> req4params = new HashMap<>();
        req4params.put("param1", Arrays.asList(new String[]{"foo"}));
        req4params.put("param2", Arrays.asList(new String[]{"10"}));
        AuthzRequestContext req4 = createAuthRequestContext("/droolsTest/foo/bar", RequestType.READ, new String[]{"role1"}, new String[]{}, AuthTestResourceParams.instance(req4params));
        Assert.assertTrue(service.isAuthorized(req4));

        // Ignored in DemoURIPolicyWorker.rule2 becaused foo<10 and /droolsTest/foo/bar is not mapped to DemoSimpleURIPolicy
        Map<String, List<String>> req5params = new HashMap<>();
        req5params.put("param1", Arrays.asList(new String[]{"foo"}));
        req5params.put("param2", Arrays.asList(new String[]{"9"}));
        AuthzRequestContext req5 = createAuthRequestContext("/droolsTest/foo/bar", RequestType.READ, new String[]{"role1"}, new String[]{}, AuthTestResourceParams.instance(req5params));
        Assert.assertFalse(service.isAuthorized(req5));
    }

    private AuthzRequestContext createAuthRequestContext(String uri, RequestType reqType,
                                                                 String[] realmRoles, String[] appRoles, ResourceParams resourceParams) {
        ResourcePath resPath = new ResourcePath(uri);
        RequestContext reqContext = new AuthTestRequestContext(reqType, resPath, resourceParams);

        Set<String> realmRolesSet = arrayToSet(realmRoles);
        Map<String, Set<String>> appRolesMap = new HashMap<>();
        appRolesMap.put(AuthzConstants.DEFAULT_APPLICATION_NAME, arrayToSet(appRoles));

        AuthToken authToken = new DefaultAuthToken(null, null, null, 0, 0, 0, null, realmRolesSet, appRolesMap);
        return new AuthzRequestContext(authToken, reqContext);
    }

    private Set<String> arrayToSet(String[] array) {
        Set<String> set = new HashSet<>();
        for (String item : array) {
            set.add(item);
        }
        return set;
    }
    */
}
