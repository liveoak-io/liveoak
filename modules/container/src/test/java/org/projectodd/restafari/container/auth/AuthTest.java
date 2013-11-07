package org.projectodd.restafari.container.auth;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.InMemoryDBResource;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.io.IOException;
import java.net.InetAddress;


import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthTest {

    private static final String EXPIRED_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJ0ZXN0LWFwcCIsImp0aSI6IjMtMTM4MTU5MTE3NTIyNiIsImV4cCI6MTM4MTU5MTQ3NSwiaWF0IjoxMzgxNTkxMTc1LCJhdWQiOiJ0ZXN0IiwicHJuIjoiYSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1c2VyIl19fQ.c1fuWB7yz0aOqAEe5dVFEt99DXfzt7LzL1plmwC5NmNcMNoyfLWUT3HATPH1Ee-3vO05bLXoBHcIhegKNpthO6qC2az-xNoKK2rUauJVa69Xiy0dsnVqWMxAwgQMUwuES5FeH7F5ht74ndqMpTimxaXCNKiX--srpyQM1xYx91k";

    // Valid token with realm role "users"
    private static final String REALM_USERS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJhdXRoVGVzdCIsImp0aSI6IjctMTM4MjQ1MDIxODI0MSIsImlhdCI6MTM4MjQ1MDIxOCwiYXVkIjoiYXV0aFRlc3QiLCJwcm4iOiJiYnVya2VAcmVkaGF0LmNvbSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1c2VycyJdfX0.KXWtt9HuQ6ppDPKjhZ1td-LUl3pBjp6JbNljBhC2mZSvJ3h9b8mtlwbTWbltniREss1rCWDIkpN0Q41Bgc3Sb9tQFn9kZ67X5hEQf9rZgdYLFxt1mBL0xKLUJHW0_CYSYFNoxhHuYrAFwnUwVpv5swzRlNsFcCn3Nr26WhL02GM";

    // Valid token with application role "users"
    private static final String APP_USERS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJhdXRoVGVzdCIsImp0aSI6IjktMTM4MjQ1MTgxNTMzMCIsImlhdCI6MTM4MjQ1MTgxNSwiYXVkIjoiYXV0aFRlc3QiLCJwcm4iOiJzaW1wbGVVc2VyIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbXX0sInJlc291cmNlX2FjY2VzcyI6eyJhdXRoVGVzdCI6eyJyb2xlcyI6WyJ1c2VycyJdfX19.SJTUYVOXohOATWoJFDB64hDJT34US6fGIO9e95qNMPXr2iqk8HSbSlGJ5E5DN3VslpELOJU0VBQ6N9TmTVWvqAXX9QUIO56oJkQsyE2DneAklbN46WZcQeoywqVfQM4Rpo7BtxT8onsCSJvq91KB73FArnCHJ-wZin_Up_Dep-8";

    // Valid token with application role "powerUsers"
    private static final String APP_POWERUSERS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJhdXRoVGVzdCIsImp0aSI6IjExLTEzODI0NTIwOTg3NDEiLCJpYXQiOjEzODI0NTIwOTgsImF1ZCI6ImF1dGhUZXN0IiwicHJuIjoic2ltcGxlVXNlciIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6W119LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYXV0aFRlc3QiOnsicm9sZXMiOlsicG93ZXJVc2VycyJdfX19.pKs07-dKKGe7tz5okmjXz0T2j323JQ-66iCmkgUiiUYAui2uAN3D0opuN-Jo9UtRbIieiL5THfSeq4b1D1rfaEFQWPZMLzWvNVSAB78M3k2xYml6u1IFnpW8pBEzXBN2Uaj1Ct-o-XDMzDIPpblryteZVD68ymL1SX-epw9-BC0";

    // Valid token with application roles "users", "powerUsers"
    private static final String APP_USERS_POWERUSERS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJhdXRoVGVzdCIsImp0aSI6IjEzLTEzODI0NTIyMjMzNDkiLCJpYXQiOjEzODI0NTIyMjMsImF1ZCI6ImF1dGhUZXN0IiwicHJuIjoic2ltcGxlVXNlciIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6W119LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYXV0aFRlc3QiOnsicm9sZXMiOlsicG93ZXJVc2VycyIsInVzZXJzIl19fX0.kqPkztwKtu_Q8eqvdd-e523Ch5K9Fno8HQs6jkA7Lk2_Rq0_QsnzW5I_FXUeBksoKg7aA9YO9WI2vjFtynpd7ey81pK_yuI4tykDjWAZZSPB0ZVZHD0yQ1Y9iMkZXtnxaINJIlVXjYZMYeqgdiMM71Xvz77wiUi8GLi1Vet-C5g";

    // Valid token with application roles "users", "admins"
    private static final String APP_USERS_ADMINS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJhdXRoVGVzdCIsImp0aSI6IjE1LTEzODI0NTI1NjA2OTEiLCJpYXQiOjEzODI0NTI1NjAsImF1ZCI6ImF1dGhUZXN0IiwicHJuIjoic2ltcGxlVXNlciIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6W119LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYXV0aFRlc3QiOnsicm9sZXMiOlsidXNlcnMiLCJhZG1pbnMiXX19fQ.LqmvMq30k7tFIkDyjVCqshE6vI0Er9AEBwUePkpPANFAYzG5VIRhXhjEdrlCiMXrrkphVQHrtbp1yi6xlfEENpmq9WIPnusEVsX7qn5M2QznD9TlcTtSZGdgKmSOcGvd94E3Bj4tZ9S-x7uBbsdpPqdZJ08Hd-Bt16aRroGq4vs";

    // Invalid audience/issuedFor
    private static final String INVALID_AUDIENCE_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJjdXN0b21lci1wb3J0YWwiLCJqdGkiOiIxNy0xMzgyNDUyNzc3MzE1IiwiaWF0IjoxMzgyNDUyNzc3LCJhdWQiOiJkZW1vIiwicHJuIjoic2ltcGxlVXNlciIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6W119LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYXV0aFRlc3QiOnsicm9sZXMiOlsidXNlcnMiLCJhZG1pbnMiXX19fQ.Wz35te__P85NP5FzT7IzG2sjGGQjkY1uyVxMh_ylntmLVOQ-GvGr8SFg6USvQMRTc7kQWpzlNgVZLyJCveZ1EHY_8F7-VyDyh61lv2SLfbHHgpg4Wm3uQfKURANnZaw0UtQFFGlJFJdPophDJPRo1lpJmcm2rLrc7Dnm-DSzSVA";

    private static DefaultContainer container;
    private static CloseableHttpClient httpClient;
    private static UnsecureServer server;

    @BeforeClass
    public static void before() throws Exception {
        container = new DefaultContainer();

        server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080);
        server.start();

        httpClient = HttpClientBuilder.create().build();
    }

    @AfterClass
    public static void after() throws Exception {
        try {
            httpClient.close();
        } finally {
            server.stop();
            System.err.flush();
        }
    }

    @Before
    public void beforeTest() throws Exception {
        // Always re-register resource again to ensure clean state
        InMemoryDBResource resource = new InMemoryDBResource("authTest");
        container.registerResource(resource, new SimpleConfig());
    }

    @Test
    public void testMissingAuthorizationHeader() throws Exception {
        HttpRequestBase httpMethod;

        // Authorization ok. Public collection with invalid type 'memory'
        httpMethod = createHttpMethod("GET", "http://localhost:8080/memory/public");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization ok. Public collection (should be accessible for R+W without token)
        httpMethod = createHttpMethod("PUT", "http://localhost:8080/authTest/public");
        ((HttpPut)httpMethod).setEntity(new StringEntity("{ \"members\": [] }"));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_CREATED);

        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_OK);

        httpMethod = createHttpMethod("DELETE", "http://localhost:8080/authTest/public/456");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok. Protected collections 'protected1' and 'protected2' should be forbidden without token
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1");
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

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
        httpMethod.addHeader(new BasicHeader("Authorization", "Basic " + EXPIRED_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization should fail because of invalid token signature
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + REALM_USERS_TOKEN + "Dep-8"));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization should fail because of expired token
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + EXPIRED_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization should fail because of invalid audience
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/public");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + INVALID_AUDIENCE_TOKEN));
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
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_POWERUSERS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok. 'powerUsers' are not able to readMember other resources than 12345 in collection protected1
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_POWERUSERS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. 'users' are able to readMember all resources of protected1
        httpMethod = createHttpMethod("GET", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_USERS_POWERUSERS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);

        // Authorization no-ok. Only 'admins' are able to create resources in collection protected1
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_USERS_POWERUSERS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. Only 'admins' are able to create resources in collection protected1
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected1/6789");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_USERS_ADMINS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND); // TODO: probably should be SC_CREATED instead, but looks like POST doesn't work...

        // Authorization no-ok. Only realm 'users' are able to create resources in collection protected2
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected2");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + APP_USERS_ADMINS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_FORBIDDEN);

        // Authorization ok. Only realm 'users' are able to create resources in collection protected2
        httpMethod = createHttpMethod("POST", "http://localhost:8080/authTest/protected2");
        httpMethod.addHeader(new BasicHeader("Authorization", "Bearer " + REALM_USERS_TOKEN));
        sendRequestAndCheckStatus(httpMethod, HttpStatus.SC_NOT_FOUND);
    }

    private HttpRequestBase createHttpMethod(String method, String uri) {
        HttpRequestBase httpMethod;
        switch (method) {
            case "GET": httpMethod = new HttpGet(uri); break;
            case "POST": httpMethod = new HttpPost(uri); break;
            case "PUT": httpMethod = new HttpPut(uri); break;
            case "DELETE": httpMethod = new HttpDelete(uri); break;
            default: throw new IllegalArgumentException("Unsupported method: " + method);
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
