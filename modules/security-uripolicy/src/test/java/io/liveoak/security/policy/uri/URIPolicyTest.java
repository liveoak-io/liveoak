/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.security.policy.uri.complex.URIPolicy;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
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
        URIPolicy uriPolicy = new URIPolicy(new DemoURIPolicyWorker());

        Set<String> johnRoles = new HashSet<>();
        johnRoles.add("role1");
        DefaultSecurityContext john = new DefaultSecurityContext();
        john.setRealm("myRealm");
        john.setSubject("john");
        john.setRoles(johnRoles);

        Set<String> evilRoles = new HashSet<>();
        evilRoles.add("evilRole");
        DefaultSecurityContext evil = new DefaultSecurityContext();
        evil.setRealm("myRealm");
        evil.setSubject("someEvilUser");
        evil.setRoles(evilRoles);

        RequestContext.Builder request1 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo")).resourceParams(ResourceParams.NONE);
        // Accepted because of rule1
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request1.securityContext(john)));
        // Rejected because of rule7, which has biggest priority and so it effectively rejects all requests of user with role 'evilRole'
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request1.securityContext(evil)));

        RequestContext.Builder request2 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar")).resourceParams(ResourceParams.NONE);
        // Ignored because there is not matching rule (Rule1 is just for /droolsTest/foo but not for /droolsTest/foo/*
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request2.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request2.securityContext(evil)));

        Map<String, List<String>> req3params = new HashMap<>();
        req3params.put("param1", Arrays.asList(new String[]{"foo"}));
        req3params.put("param2", Arrays.asList(new String[]{"11"}));
        RequestContext.Builder request3 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar")).resourceParams(DefaultResourceParams.instance(req3params));
        // Accepted because of rule2 (Both URI and parameter conditions match)
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request3.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request3.securityContext(evil)));

        Map<String, List<String>> req4params = new HashMap<>();
        req4params.put("param1", Arrays.asList(new String[]{"foo"}));
        req4params.put("param2", Arrays.asList(new String[]{"9"}));
        RequestContext.Builder request4 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar")).resourceParams(DefaultResourceParams.instance(req4params));
        // Ignored. Doesn't match rule2 because param2 is lower than 10
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request4.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request4.securityContext(evil)));

        Map<String, List<String>> req5params = new HashMap<>();
        req5params.put("param1", Arrays.asList(new String[]{"foo"}));
        req5params.put("param2", Arrays.asList(new String[]{"baz"}));
        req5params.put("param3", Arrays.asList(new String[]{"john"}));
        RequestContext.Builder request5 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar/baz")).resourceParams(DefaultResourceParams.instance(req5params));
        // Accepted because of rule3
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request5.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request5.securityContext(evil)));

        Map<String, List<String>> req6params = new HashMap<>();
        req6params.put("param1", Arrays.asList(new String[]{"foo"}));
        req6params.put("param2", Arrays.asList(new String[]{"baz"}));
        req6params.put("param3", Arrays.asList(new String[]{"mary"}));
        RequestContext.Builder request6 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar/baz")).resourceParams(DefaultResourceParams.instance(req6params));
        // Ignored. Doesn't match rule3 because param3 has different value than actual username (john)
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request6.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request6.securityContext(evil)));

        Map<String, List<String>> req7params = new HashMap<>();
        req7params.put("param1", Arrays.asList(new String[]{"foo"}));
        req7params.put("param2", Arrays.asList(new String[]{"baaz"}));
        req7params.put("param3", Arrays.asList(new String[]{"john"}));
        RequestContext.Builder request7 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/foo/bar/baz")).resourceParams(DefaultResourceParams.instance(req7params));
        // Ignored. Doesn't match rule3 because param2 has different value than the parsed value from regex from URI (baz)
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request7.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request7.securityContext(evil)));

        RequestContext.Builder request8 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/john")).resourceParams(ResourceParams.NONE);
        // Accepted because of rule4, which allows every user to visit URI like "/droolsTest/foo" if his username is "foo"
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request8.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request8.securityContext(evil)));

        RequestContext.Builder request9 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/mary")).resourceParams(ResourceParams.NONE);
        // Ignored. Doesn't match rule4 or any other rule
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request9.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request9.securityContext(evil)));

        RequestContext.Builder request10 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/role1")).resourceParams(ResourceParams.NONE);
        // Accepted because of rule5, which allows any user to visit URI like '/droolsTest/foo' if he has realmRole 'foo'
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request10.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request10.securityContext(evil)));

        RequestContext.Builder request11 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/role2")).resourceParams(ResourceParams.NONE);
        // Ignored.
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request11.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request11.securityContext(evil)));

        // Similar to request1 but with RequestType.CREATE. Now it's rejected because of rule6
        RequestContext.Builder request12 = new RequestContext.Builder().requestType(RequestType.CREATE)
                .resourcePath(new ResourcePath("/droolsTest/foo")).resourceParams(ResourceParams.NONE);
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request12.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request12.securityContext(evil)));


        // Request allowed thanks to rule8 and sort parameter
        Map<String, List<String>> req13params = new HashMap<>();
        req13params.put("sort", Arrays.asList(new String[]{"user,name"}));
        req13params.put("limit", Arrays.asList(new String[]{"10"}));
        RequestContext.Builder request13 = new RequestContext.Builder().requestType(RequestType.READ)
                .resourcePath(new ResourcePath("/droolsTest/bar")).resourceParams(DefaultResourceParams.instance(req13params));
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request13.securityContext(john)));
        Assert.assertEquals(AuthzDecision.REJECT, uriPolicy.isAuthorized(request13.securityContext(evil)));

        // Should pass because of limit==5
        req13params.put("sort", Arrays.asList(new String[]{"user,namee"}));
        req13params.put("limit", Arrays.asList(new String[]{"5"}));
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request13.securityContext(john)));


        // Should pass because of q
        req13params.put("sort", Arrays.asList(new String[]{"user,namee"}));
        req13params.put("limit", Arrays.asList(new String[]{"10"}));
        req13params.put("q", Arrays.asList(new String[]{"{\"completed\":false}"}));
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request13.securityContext(john)));

        // Shouldn't pass because anything from sort,limit or q can be applied
        req13params.put("sort", Arrays.asList(new String[]{"user,namee"}));
        req13params.put("limit", Arrays.asList(new String[]{"10"}));
        req13params.put("q", Arrays.asList(new String[]{"{\"completed\":\"kokos\"}"}));
        Assert.assertEquals(AuthzDecision.IGNORE, uriPolicy.isAuthorized(request13.securityContext(john)));

        // This is passing thanks to rule9
        req13params.put("q", Arrays.asList(new String[]{"{\"completed\":true}"}));
        Assert.assertEquals(AuthzDecision.ACCEPT, uriPolicy.isAuthorized(request13.securityContext(john)));
    }
}
