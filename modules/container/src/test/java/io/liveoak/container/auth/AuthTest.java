/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.auth;

import io.liveoak.container.DefaultContainer;
import io.liveoak.container.InMemoryDBResource;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.server.UnsecureServer;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.Server;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthTest {

    private static LiveOakSystem system;
    private static Container container;
    private static CloseableHttpClient httpClient;
    private static Server server;

    @BeforeClass
    public static void before() throws Exception {
        system = LiveOakFactory.create();
        container = system.container();

        server = system.server( "unsecure" );
        httpClient = HttpClientBuilder.create().build();
    }

    @AfterClass
    public static void after() throws Exception {
        try {
            httpClient.close();
        } finally {
            system.stop();
            System.err.flush();
        }
    }

    @Before
    public void beforeTest() throws Exception {
        // Always re-register resource again to ensure clean state
        InMemoryDBResource resource = new InMemoryDBResource("authTest");
        container.registerResource(resource);
    }

    @Test
    public void testMissingAuthorizationHeader() throws Exception {
        HttpRequestBase httpMethod;

        System.err.println("A");
        // Authorization ok. Public collection with invalid type 'memory'
        httpMethod = createHttpMethod("GET", "http://localhost:8080/memory/public");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);
        System.err.println("B");

        // Authorization ok. Public collection (should be accessible for R+W without token)
        httpMethod = createHttpMethod("PUT", "http://localhost:8080/authTest/public");
        ((HttpPut) httpMethod).setEntity(new StringEntity("{ \"members\": [] }"));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_CREATED);
        System.err.println("C");

        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);
        System.err.println("D");

        httpMethod = createHttpMethod("DELETE", "http://localhost:8080/authTest/public/456");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);
        System.err.println("E");

        // Authorization no-ok. Protected collections 'protected1' and 'protected2' should be forbidden without token
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);
        System.err.println("F");

        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected1");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected2");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. Collection 'protected2' is available for readMember without token
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected2");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);
    }


    @Test
    public void testInvalidAuthorizationHeader() throws Exception {
        HttpRequestBase httpMethod;

        // Authorization header ignored as it's type is 'Basic'. Resource is public, so ignored header means that authorization is ok
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Basic " + TestAuthConstants.EXPIRED_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization should fail because of invalid token signature
        // TODO: Uncomment this. Signature verification should be revisited (Maybe needs to be done with BouncyCastle...)
        /*httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.INVALID_SIGNATURE_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);*/

        // Authorization should fail because of expired token
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.EXPIRED_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization should fail because of invalid realm
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.INVALID_AUDIENCE_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization should fail because of invalid application
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.INVALID_ISSUEDFOR_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization should fail because of null user
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.NULLUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);
    }


    @Test
    public void testToken() throws Exception {
        HttpRequestBase httpMethod;

        // Authorization ok. Public resource.
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public/12345");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization ok. 'powerUsers' are able to readMember 12345
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1/12345");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPPOWERUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok. 'powerUsers' are not able to readMember other resources than 12345 in collection protected1
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPPOWERUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. 'users' are able to readMember all resources of protected1
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPUSER_APPPOWERUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok. Only 'admins' are able to create resources in collection protected1
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPUSER_APPPOWERUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. Only 'admins' are able to create resources in collection protected1
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPUSER_APPADMIN_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND); // TODO: probably should be SC_CREATED instead, but looks like POST doesn't work...

        // Authorization no-ok. Only realm 'users' are able to create resources in collection protected2
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected2");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_APPUSER_APPADMIN_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. Only realm 'users' are able to create resources in collection protected2
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected2");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + TestAuthConstants.JOHN_REALMUSER_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testComplexPolicy() throws Exception {
        HttpRequestBase httpMethod;

        // Authorization ok. rule2 of DemoURIPolicy
        httpMethod = createHttpMethod("GET", "http://localhost:8080/droolsTest/foo/bar?param1=foo&param2=10");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok because of param1
        httpMethod = createHttpMethod("GET", "http://localhost:8080/droolsTest/foo/bar?param1=noFoooooo&param2=10");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization no-ok because of param2
        httpMethod = createHttpMethod("GET", "http://localhost:8080/droolsTest/foo/bar?param1=foo&param2=9");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);
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
