/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.keycloak;

import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.SecurityContext;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.liveoak.testtools.MockExtension;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.SkeletonKeyToken;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class AuthHandlerTest extends AbstractResourceTestCase {

    private static CloseableHttpClient httpClient;
    private static TokenUtil tokenUtil;

    private MockRootResource mock;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "auth", new KeycloakExtension() );
        loadExtension( "auth-test", new MockExtension( MockRootResource.class ) );
    }

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "/src/test/resources" );
    }

    @Before
    public void before() throws Exception {
        tokenUtil = new TokenUtil((RealmModel) this.system.service( KeycloakServices.realmModel("testOrg", "testApp") ));
        httpClient = HttpClientBuilder.create().build();

        mock = (MockRootResource) this.system.service( MockExtension.resource("testOrg", "testApp", MockRootResource.class) );
    }

    @After
    public void after() throws Exception {
        httpClient.close();
    }

    @Test(timeout = 10000)
    public void testNoAuth() throws Exception {
        HttpRequestBase httpMethod = createHttpMethod("GET", "http://localhost:8080/testOrg/testApp/auth-test");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);

        RequestContext context = mock.pollRequest(2, TimeUnit.SECONDS);
        Assert.assertFalse(context.securityContext().isAuthenticated());
    }

    @Test(timeout = 10000)
    public void testAuth() throws Exception {
        SkeletonKeyToken token = tokenUtil.createToken();

        HttpRequestBase httpMethod = createHttpMethod("GET", "http://localhost:8080/testOrg/testApp/auth-test");
        httpMethod.addHeader(new BasicHeader("Authorization", "bearer " + tokenUtil.toString(token)));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);

        SecurityContext context = mock.pollRequest(10, TimeUnit.SECONDS).securityContext();
        Assert.assertTrue(context.isAuthenticated());
        Assert.assertEquals("testOrg-testApp", context.getRealm());
        Assert.assertEquals("user-id", context.getSubject());
        Assert.assertEquals(3, context.getRoles().size());
        Assert.assertEquals(token.getIssuedAt(), context.lastVerified());
    }

    @Test(timeout = 10000)
    public void testAuthExpired() throws Exception {
        SkeletonKeyToken token = tokenUtil.createToken();
        token.expiration((System.currentTimeMillis() / 1000) - 10);

        HttpRequestBase httpMethod = createHttpMethod("GET", "http://localhost:8080/testOrg/testApp/auth-test");
        httpMethod.addHeader(new BasicHeader("Authorization", "bearer " + tokenUtil.toString(token)));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(timeout = 10000)
    public void testInvalidAuth() throws Exception {
        HttpRequestBase httpMethod = createHttpMethod("GET", "http://localhost:8080/testOrg/testApp/auth-test");
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
        assertEquals(expectedStatusCode, resp.getStatusLine().getStatusCode());
        resp.close();
    }
}
