package io.liveoak.security.policy.uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.policy.uri.impl.URIPolicy;
import io.liveoak.security.policy.uri.impl.URIPolicyConfigurator;
import io.liveoak.security.policy.uri.integration.URIPolicyConfig;
import io.liveoak.security.policy.uri.integration.URIPolicyConfigRule;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class URIPolicyTest {

    private URIPolicy uriPolicy;

    @Before
    public void before() {
        List<URIPolicyConfigRule> rules = new ArrayList<>();

        rules.add(createRule("/authTest/public*", Arrays.asList("*"), null, null, Arrays.asList("*"), null));

        rules.add(createRule("/authTest/protected1/*", Arrays.asList(RequestType.READ.toString()), Arrays.asList("user"), null, null, null));

        rules.add(createRule("/authTest/protected1/*", Arrays.asList("*"), Arrays.asList("admin"), null, null, null));

        rules.add(createRule("/authTest/protected1/12345", Arrays.asList("*"), Arrays.asList("user", "powerUser"), null, null, null));

        rules.add(createRule("/authTest/protected2/12345", Arrays.asList(RequestType.CREATE.toString()), Arrays.asList("user"), null, null, null));

        URIPolicyConfig config = new URIPolicyConfig();
        config.setRules(rules);
        this.uriPolicy = new URIPolicy();
        new URIPolicyConfigurator().configure(uriPolicy, config);
    }

    private URIPolicyConfigRule createRule(String uriPattern, List<String> requestTypes, List<String> allowedRoles, List<String> deniedRoles,
                                           List<String> allowedUsers, List<String> deniedUsers) {
        URIPolicyConfigRule rule = new URIPolicyConfigRule();
        rule.setUriPattern(uriPattern);
        rule.setRequestTypes(requestTypes);
        rule.setAllowedRoles(allowedRoles);
        rule.setDeniedRoles(deniedRoles);
        rule.setAllowedUsers(allowedUsers);
        rule.setDeniedUsers(deniedUsers);
        return rule;
    };

    @Test
    public void testUriPolicy() {
        RequestContext req1 = createRequestContext("/something", RequestType.READ, new String[]{});
        Assert.assertTrue(uriPolicy.isAuthorized(req1) == AuthzDecision.IGNORE);

        RequestContext req2 = createRequestContext("/authTest/public/12345", RequestType.READ,
                new String[]{});
        Assert.assertTrue(uriPolicy.isAuthorized(req2) == AuthzDecision.ACCEPT);

        RequestContext req3 = createRequestContext("/authTest/protected1/12345", RequestType.READ,
                new String[]{"powerUser"});
        Assert.assertTrue(uriPolicy.isAuthorized(req3) == AuthzDecision.ACCEPT);

        RequestContext req4 = createRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[]{"powerUser"});
        Assert.assertTrue(uriPolicy.isAuthorized(req4) == AuthzDecision.IGNORE);

        RequestContext req5 = createRequestContext("/authTest/protected1/6789", RequestType.READ,
                new String[]{"user", "powerUser"});
        Assert.assertTrue(uriPolicy.isAuthorized(req5) == AuthzDecision.ACCEPT);

        RequestContext req6 = createRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[]{"user", "powerUser"});
        Assert.assertTrue(uriPolicy.isAuthorized(req6) == AuthzDecision.IGNORE);

        RequestContext req7 = createRequestContext("/authTest/protected1/6789", RequestType.CREATE,
                new String[]{"user", "admin"});
        Assert.assertTrue(uriPolicy.isAuthorized(req7) == AuthzDecision.ACCEPT);

        RequestContext req8 = createRequestContext("/authTest/protected2", RequestType.CREATE,
                new String[]{"user", "admin"});
        Assert.assertTrue(uriPolicy.isAuthorized(req8) == AuthzDecision.IGNORE);

        RequestContext req10 = createRequestContext("/authTest/protected2", RequestType.READ,
                new String[]{});
        Assert.assertTrue(uriPolicy.isAuthorized(req10) == AuthzDecision.IGNORE);

        RequestContext req11 = createRequestContext("/authTest/public", RequestType.READ,
                new String[]{});
        Assert.assertTrue(uriPolicy.isAuthorized(req11) == AuthzDecision.ACCEPT);
    }

    private RequestContext createRequestContext(String uri, RequestType reqType, String[] roles) {
        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.setSubject("joe");
        securityContext.setRoles(new HashSet<>(Arrays.asList(roles)));

        return new RequestContext.Builder()
                .resourcePath(new ResourcePath(uri))
                .requestType(reqType)
                .securityContext(securityContext);
    }
}
