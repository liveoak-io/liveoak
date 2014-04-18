package io.liveoak.security.policy.uri;

import java.util.Arrays;
import java.util.HashSet;

import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.security.AuthzDecision;
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
        uriPolicy = new URIPolicy();
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/public"), Arrays.asList("*"), null, null, Arrays.asList("*"), null);
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/public/*"), Arrays.asList("*"), null, null, Arrays.asList("*"), null);
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/protected1/*"), Arrays.asList(RequestType.READ.toString()), Arrays.asList("user"), null, null, null);
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/protected1/*"), Arrays.asList("*"), Arrays.asList("admin"), null, null, null);
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/protected1/12345"), Arrays.asList("*"), Arrays.asList("user", "powerUser"), null, null, null);
        uriPolicy.addURIPolicyRule(new ResourcePath("/authTest/protected2/12345"), Arrays.asList(RequestType.CREATE.toString()), Arrays.asList("user"), null, null, null);
    }

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
