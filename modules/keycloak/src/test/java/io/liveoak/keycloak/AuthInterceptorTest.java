/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.keycloak;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.testtools.MockExtension;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.representations.AccessToken;

import static org.fest.assertions.Assertions.assertThat;

public class AuthInterceptorTest extends AbstractKeycloakTest {

    private static CloseableHttpClient httpClient;
    private static TokenUtil tokenUtil;

    private MockRootResource mock;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("auth", new KeycloakExtension(), createTestConfig());
        loadExtension("auth-test", new MockExtension(MockRootResource.class));
        setupAuthInterceptor();
        installTestAppResource("auth", "auth", JsonNodeFactory.instance.objectNode());
        installTestAppResource("auth-test", "auth-test", JsonNodeFactory.instance.objectNode());
    }

    @Before
    public void before() throws Exception {
        System.err.println("** A");
        tokenUtil = new TokenUtil("liveoak-apps");
        System.err.println("** B");
        httpClient = HttpClientBuilder.create().build();
        System.err.println("** C");

        mock = (MockRootResource) system.service(Services.resource("testApp", "auth-test"));
    }

    @After
    public void after() throws Exception {
        httpClient.close();
    }

    @Test(timeout = 10000)
    public void authInterceptorTests() throws Exception {
        // Test #1 - No auth
        HttpRequestBase httpMethod = createHttpMethod("GET", "http://localhost:8080/testApp/auth-test");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);

        RequestContext context = mock.pollRequest(2, TimeUnit.SECONDS);
        assertThat(context.securityContext().isAuthenticated()).isFalse();


        // Test #2 - Auth
        System.err.println("******************");
        AccessToken token = tokenUtil.createToken();

        httpMethod = createHttpMethod("GET", "http://localhost:8080/testApp/auth-test");
        httpMethod.addHeader(new BasicHeader("Authorization", "bearer " + tokenUtil.toString(token)));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);

        SecurityContext securityContext = mock.pollRequest(10, TimeUnit.SECONDS).securityContext();
        assertThat(securityContext.isAuthenticated()).isTrue();
        assertThat(securityContext.getRealm()).isEqualTo("liveoak-apps");
        assertThat(securityContext.getSubject()).isEqualTo("user-id");
        assertThat(securityContext.getUser()).isNotNull();
        assertThat(securityContext.getUser().givenName()).isEqualTo("given");
        assertThat(securityContext.getUser().familyName()).isEqualTo("family");
        assertThat(securityContext.getUser().email()).isEqualTo("email");
        assertThat(securityContext.getRoles().size()).isEqualTo(3);
        assertThat(securityContext.lastVerified()).isEqualTo(token.getIssuedAt());


        // Test #3 - Auth expired
        token = tokenUtil.createToken();
        token.expiration((int) ((System.currentTimeMillis() / 1000) - 10));

        httpMethod = createHttpMethod("GET", "http://localhost:8080/testApp/auth-test");
        httpMethod.addHeader(new BasicHeader("Authorization", "bearer " + tokenUtil.toString(token)));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_UNAUTHORIZED);


        // Test #4 - Invalid auth
        httpMethod = createHttpMethod("GET", "http://localhost:8080/testApp/auth-test");
        httpMethod.addHeader(new BasicHeader("Authorization", "bearer invalid-token"));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_UNAUTHORIZED);
    }

    private HttpRequestBase createHttpMethod(String method, String uri) {
        HttpRequestBase httpMethod;
        switch (method) {
            case "GET":
                httpMethod = new HttpGet(uri);
                break;
            case "POST":
                httpMethod = new HttpPost(uri);
                break;
            case "PUT":
                httpMethod = new HttpPut(uri);
                break;
            case "DELETE":
                httpMethod = new HttpDelete(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        httpMethod.addHeader(new BasicHeader("Accept", "application/json"));
        httpMethod.addHeader(new BasicHeader("Content-Type", "application/json"));
        return httpMethod;
    }

    private void sendRequestAndCheckStatus(HttpRequestBase req, int expectedStatusCode) throws IOException {
        CloseableHttpResponse resp = httpClient.execute(req);
        assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(expectedStatusCode);
        resp.close();
    }

    private static void setupAuthInterceptor() throws Exception {
        ObjectNode interceptorConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode httpChainConfig = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "auth");
        interceptorConfig.putArray("http").add(httpChainConfig);
        loadExtension("interceptor", new InterceptorExtension(), interceptorConfig);
    }
}
