/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.keycloak;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.jpa.JpaKeycloakSessionFactory;
import org.keycloak.models.mongo.keycloak.adapters.MongoKeycloakSessionFactory;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KeycloakRestartTest extends AbstractResourceTestCase {

    private CloseableHttpClient httpClient;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "auth", new KeycloakExtension(), createKeycloakConfig("mongo"));
    }

    @Before
    public void before() throws Exception {
        httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @Test
    public void testRestart() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();

        // First check that Keycloak is available under localhost:8383 and KeycloakSessionFactory is instance of Mongo
        KeycloakSessionFactory sessionFactory = (KeycloakSessionFactory)system.service(KeycloakServices.sessionFactory("auth"));
        Assert.assertTrue(sessionFactory instanceof MongoKeycloakSessionFactory);
        Assert.assertTrue(isUriAvailable("localhost", 8383));

        // Update keycloak configuration to use localhost:8384 and JPA
        client.update(requestContext, "/admin/system/auth", ConversionUtils.convert(createKeycloakConfig("jpa")));

        sessionFactory = (KeycloakSessionFactory)system.service(KeycloakServices.sessionFactory("auth"));

        Assert.assertTrue(sessionFactory instanceof JpaKeycloakSessionFactory);
        Assert.assertTrue(isUriAvailable("localhost", 8383));

        // Update just model to be Mongo, but keep same host,port
        client.update(requestContext, "/admin/system/auth", ConversionUtils.convert(createKeycloakConfig("mongo")));

        // Verify instance of KeycloakSessionFactory is changed to mongo
        sessionFactory = (KeycloakSessionFactory)system.service(KeycloakServices.sessionFactory("auth"));
        Assert.assertTrue(sessionFactory instanceof MongoKeycloakSessionFactory);
        Assert.assertTrue(isUriAvailable("localhost", 8383));
    }

    private boolean isUriAvailable(String host, int port) throws IOException {
        String uri = "http://" + host + ":" + port + "/auth/admin";
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (HttpHostConnectException exception) {
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private ObjectNode createKeycloakConfig(String model) {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put(KeycloakSystemResource.MODEL, model);
        return config;
    }




}
