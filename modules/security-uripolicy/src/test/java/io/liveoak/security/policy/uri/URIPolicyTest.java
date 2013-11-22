/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.security.impl.DefaultAuthToken;
import io.liveoak.security.policy.uri.complex.DemoURIPolicy;
import io.liveoak.security.spi.AuthToken;
import io.liveoak.security.spi.AuthorizationDecision;
import io.liveoak.security.spi.AuthorizationRequestContext;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyTest {

    @Test
    public void testPolicy() {
        DemoURIPolicy uriPolicy = new DemoURIPolicy();
        uriPolicy.init();

        Set<String> johnRealmRoles = new HashSet<>();
        johnRealmRoles.add("role1");
        Map<String, Set<String>> johnAppRoles = new HashMap<>();
        johnAppRoles.put("myApplication", Collections.EMPTY_SET);
        AuthToken john = new DefaultAuthToken("john", "myRealm", "myApplication", 0, 0, 0, "some", johnRealmRoles, johnAppRoles);

        Set<String> evilRealmRoles = new HashSet<>();
        evilRealmRoles.add("evilRole");
        Map<String, Set<String>> evilAppRoles = new HashMap<>();
        evilAppRoles.put("myApplication", Collections.EMPTY_SET);
        AuthToken evil = new DefaultAuthToken("someEvilUser", "myRealm", "myApplication", 0, 0, 0, "some", evilRealmRoles, evilAppRoles);

        RequestContext request1 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo"), ResourceParams.NONE);
        // Accepted because of rule1
        Assert.assertEquals(AuthorizationDecision.ACCEPT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request1)));
        // Rejected because of rule7, which has biggest priority and so it effectively rejects all requests of user with role 'evilRole'
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request1)));

        RequestContext request2 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar"), ResourceParams.NONE);
        // Ignored because there is not matching rule (Rule1 is just for /droolsTest/foo but not for /droolsTest/foo/*
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request2)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request2)));

        Map<String, List<String>> req3params = new HashMap<>();
        req3params.put("param1", Arrays.asList(new String[]{"foo"}));
        req3params.put("param2", Arrays.asList(new String[]{"11"}));
        RequestContext request3 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar"), AuthTestResourceParams.instance(req3params));
        // Accepted because of rule2 (Both URI and parameter conditions match)
        Assert.assertEquals(AuthorizationDecision.ACCEPT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request3)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request3)));

        Map<String, List<String>> req4params = new HashMap<>();
        req4params.put("param1", Arrays.asList(new String[]{"foo"}));
        req4params.put("param2", Arrays.asList(new String[]{"9"}));
        RequestContext request4 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar"), AuthTestResourceParams.instance(req4params));
        // Ignored. Doesn't match rule2 because param2 is lower than 10
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request4)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request4)));

        Map<String, List<String>> req5params = new HashMap<>();
        req5params.put("param1", Arrays.asList(new String[]{"foo"}));
        req5params.put("param2", Arrays.asList(new String[]{"baz"}));
        req5params.put("param3", Arrays.asList(new String[]{"john"}));
        RequestContext request5 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar/baz"), AuthTestResourceParams.instance(req5params));
        // Accepted because of rule3
        Assert.assertEquals(AuthorizationDecision.ACCEPT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request5)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request5)));

        Map<String, List<String>> req6params = new HashMap<>();
        req6params.put("param1", Arrays.asList(new String[]{"foo"}));
        req6params.put("param2", Arrays.asList(new String[]{"baz"}));
        req6params.put("param3", Arrays.asList(new String[]{"mary"}));
        RequestContext request6 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar/baz"), AuthTestResourceParams.instance(req6params));
        // Ignored. Doesn't match rule3 because param3 has different value than actual username (john)
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request6)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request6)));

        Map<String, List<String>> req7params = new HashMap<>();
        req7params.put("param1", Arrays.asList(new String[]{"foo"}));
        req7params.put("param2", Arrays.asList(new String[]{"baaz"}));
        req7params.put("param3", Arrays.asList(new String[]{"john"}));
        RequestContext request7 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/foo/bar/baz"), AuthTestResourceParams.instance(req7params));
        // Ignored. Doesn't match rule3 because param2 has different value than the parsed value from regex from URI (baz)
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request7)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request7)));

        RequestContext request8 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/john"), ResourceParams.NONE);
        // Accepted because of rule4, which allows every user to visit URI like "/droolsTest/foo" if his username is "foo"
        Assert.assertEquals(AuthorizationDecision.ACCEPT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request8)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request8)));

        RequestContext request9 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/mary"), ResourceParams.NONE);
        // Ignored. Doesn't match rule4 or any other rule
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request9)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request9)));

        RequestContext request10 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/role1"), ResourceParams.NONE);
        // Accepted because of rule5, which allows any user to visit URI like '/droolsTest/foo' if he has realmRole 'foo'
        Assert.assertEquals(AuthorizationDecision.ACCEPT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request10)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request10)));

        RequestContext request11 = new AuthTestRequestContext(RequestType.READ, new ResourcePath("/droolsTest/role2"), ResourceParams.NONE);
        // Ignored.
        Assert.assertEquals(AuthorizationDecision.IGNORE, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request11)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request11)));

        // Similar to request1 but with RequestType.CREATE. Now it's rejected because of rule6
        RequestContext request12 = new AuthTestRequestContext(RequestType.CREATE, new ResourcePath("/droolsTest/foo"), ResourceParams.NONE);
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(john, request12)));
        Assert.assertEquals(AuthorizationDecision.REJECT, uriPolicy.isAuthorized(new AuthorizationRequestContext(evil, request12)));

    }
}
