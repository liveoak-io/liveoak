package io.liveoak.redirect.https;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.redirect.https.resource.Redirect;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.http.HttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectTestCase extends BaseHttpsRedirectTestCase {

    @BeforeClass
    public static void setupTests() throws Exception {
        ResourceState foo = new DefaultResourceState("foo");
        foo.putProperty("name", "mr foo");
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", foo);

        ResourceState bar = new DefaultResourceState("bar");
        bar.putProperty("name", "mr bar");
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", bar);
    }

    @Test
    public void testRedirectAllTemporary() throws Exception {
        // check redirecting all, temporary, and with a max time of 10 seconds
        setAppRedirect(Redirect.Options.ALL.toString(), Redirect.Types.TEMPORARY.toString(), 10);

        // with http
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(foo, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/foo", "max-age=10");

        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=10");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectAllPermanent() throws Exception {
        //check redirecting all, permanent, and with a max time of 123 seconds
        setAppRedirect(Redirect.Options.ALL.toString(), Redirect.Types.PERMANENT.toString(), 123);

        // with http
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(foo, HttpResponseStatus.MOVED_PERMANENTLY.code(), HttpResponseStatus.MOVED_PERMANENTLY.reasonPhrase(), "https://localhost:8080/testApp/mock/foo", "max-age=123");

        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.MOVED_PERMANENTLY.code(), HttpResponseStatus.MOVED_PERMANENTLY.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=123");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectNoneTemporary() throws Exception {
        // check redirecting all, temporary, and with a max time of 10 seconds
        setAppRedirect(Redirect.Options.NONE.toString(), Redirect.Types.TEMPORARY.toString(), 10);

        // with http
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectNonePermanent() throws Exception {
        //check redirecting all, permanent, and with a max time of 123 seconds
        setAppRedirect(Redirect.Options.NONE.toString(), Redirect.Types.PERMANENT.toString(), 123);

        // with http
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());

        // with https
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectSecuredTemporary() throws Exception {
        // check redirecting all, temporary, and with a max time of 10 seconds
        setAppRedirect(Redirect.Options.SECURED.toString(), Redirect.Types.TEMPORARY.toString(), 10);

        // with http
        // foo is unsecured, so should not perform a redirect
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        // bar is secured, so should perform a redirect
        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=10");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectSecuredPermanent() throws Exception {
        //check redirecting all, permanent, and with a max time of 123 seconds
        setAppRedirect(Redirect.Options.SECURED.toString(), Redirect.Types.PERMANENT.toString(), 123);

        // with http
        // foo is unsecured, so should not perform a redirect
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        // bar is secured, so should perform a redirect
        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.MOVED_PERMANENTLY.code(), HttpResponseStatus.MOVED_PERMANENTLY.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=123");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectAuthorizedHeaderTemporary() throws Exception {
        // check redirecting all, temporary, and with a max time of 10 seconds
        setAppRedirect(Redirect.Options.SECURED.toString(), Redirect.Types.TEMPORARY.toString(), 10);

        // with http
        // foo is unsecured, but we are passing an authorization header
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").addHeader("authorization", "letmein").execute();
        checkRedirectResponse(foo, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/foo", "max-age=10");

        // bar is secured, so should perform a redirect
        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").addHeader("authorization", "letmein").execute();
        checkRedirectResponse(bar, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=10");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testRedirectAuthorizedHeaderPermanent() throws Exception {
        //check redirecting all, permanent, and with a max time of 123 seconds
        setAppRedirect(Redirect.Options.SECURED.toString(), Redirect.Types.PERMANENT.toString(), 123);

        // with http
        // foo is unsecured, but we are passing an authorization header
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").addHeader("authorization", "letmein").execute();
        checkRedirectResponse(foo, HttpResponseStatus.MOVED_PERMANENTLY.code(), HttpResponseStatus.MOVED_PERMANENTLY.reasonPhrase(), "https://localhost:8080/testApp/mock/foo", "max-age=123");

        // bar is secured, so should perform a redirect
        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").addHeader("authorization", "letmein").execute();
        checkRedirectResponse(bar, HttpResponseStatus.MOVED_PERMANENTLY.code(), HttpResponseStatus.MOVED_PERMANENTLY.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=123");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }

    @Test
    public void testDefaults() throws Exception {
        // set the redirect values to null, this should force the system to use the system level defaults.
        // system defaults (see MockExtension) SECURED, TEMPORARY, max-time = 300 seconds
        setAppRedirect(null, null, null);

        // with http
        // foo is unsecured, so should not perform a redirect
        HttpResponse foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        // bar is secured, so should perform a redirect
        HttpResponse bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=300");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());


        setAppRedirect(Redirect.Options.ALL.toString(), null, null);
        // with http
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(foo, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/foo", "max-age=300");

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "http").execute();
        checkRedirectResponse(bar, HttpResponseStatus.FOUND.code(), HttpResponseStatus.FOUND.reasonPhrase(), "https://localhost:8080/testApp/mock/bar", "max-age=300");

        // with https, eg no redirect should occur
        foo = get("/testApp/mock/foo").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(foo, HttpResponseStatus.OK.code());

        bar = get("/testApp/mock/bar").addHeader("X-Forwarded-Proto", "https").execute();
        checkResponse(bar, HttpResponseStatus.UNAUTHORIZED.code());
    }


    private void checkRedirectResponse(HttpResponse response, int statusCode, String reasonPhrase, String location, String cacheControl) {
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getStatusLine().getReasonPhrase()).isEqualTo(reasonPhrase);
        assertThat(response.getFirstHeader("Location").getValue()).isEqualTo(location);

        if (response.getFirstHeader("cache-control") == null)  {
            if (cacheControl != null) {
                fail();
            }
        } else {
            assertThat(response.getFirstHeader("cache-control").getValue()).isEqualTo(cacheControl);
        }
    }

    private void checkResponse(HttpResponse response, int statusCode) {
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
    }

    private void setAppRedirect(String redirects, String type, Integer maxAge) throws Exception{
        String path = "/admin/applications/testApp/resources/https-redirect";
        ResourceState redirectConfig = client.read(new RequestContext.Builder().build(), path);

        redirectConfig.putProperty(Redirect.REDIRECT, redirects);
        redirectConfig.putProperty(Redirect.TYPE, type);
        redirectConfig.putProperty(Redirect.MAX_AGE, maxAge);

        client.update(new RequestContext.Builder().build(), path, redirectConfig);
    }

}
