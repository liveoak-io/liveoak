/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.vertx.java.core.Vertx;

import static org.fest.assertions.Assertions.assertThat;


/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public abstract class AbstractHTTPResourceTestCase extends AbstractTestCase {

    protected LiveOakSystem system;
    protected Client client;
    protected CloseableHttpClient httpClient;
    protected InternalApplication application;

    private Set<String> extensionIds = new HashSet<>();
    private Set<InternalApplicationExtension> extensions = new HashSet<>();
    protected Vertx vertx;

    public abstract void loadExtensions() throws Exception;

    protected File applicationDirectory() {
        return null;
    }

    protected void loadExtension(String id, Extension ext) throws Exception {
        this.system.extensionInstaller().load(id, ext);
        this.extensionIds.add(id);
    }

    protected void loadExtension(String id, Extension ext, ObjectNode config) throws Exception {
        ObjectNode fullConfig = JsonNodeFactory.instance.objectNode();
        fullConfig.put( "config", config );
        this.system.extensionInstaller().load(id, ext, fullConfig);
        this.extensionIds.add(id);
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ObjectNode resourceConfig) throws Exception {
        InternalApplicationExtension appExt = this.application.extend(extId, resourceId, resourceConfig);
        this.extensions.add(appExt);
        return appExt;
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ResourceState resourceConfig) throws Exception {
        return installResource( extId, resourceId, ConversionUtils.convert(resourceConfig) );
    }

    protected ResourceState toResourceState(HttpEntity entity, MediaType contentType) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        entity.writeTo(out);
        out.flush();
        out.close();
        return this.system.codecManager().decode(contentType, buffer);
    }

    protected ResourceState jsonToResourceState(HttpEntity entity) throws Exception {
        return toResourceState(entity, MediaType.JSON);
    }

    protected ResourceState toResourceState(ByteBuf buffer, MediaType contentType) throws Exception {
        return this.system.codecManager().decode(contentType, buffer);
    }

    protected ResourceState jsonToResourceState(ByteBuf buffer) throws Exception {
        return toResourceState(buffer, MediaType.JSON);
    }

    protected HttpEntity httpGet(String uri) throws Exception {
        return httpGet(uri, 200);
    }

    protected HttpEntity httpGet(String uri, int statusCode) throws Exception {
        HttpGet get = new HttpGet(uri);
        get.addHeader(HttpHeaders.Names.ACCEPT, MediaType.JSON.toString());

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
            HttpEntity entity = response.getEntity();
            assertThat(entity).isNotNull();
            return entity;
        }
    }

    protected String getResourceAsString(String uri) throws Exception {
        return EntityUtils.toString(httpGet(uri));
    }

    protected JsonNode getResourceAsJson(String uri) throws Exception {
        return toJSON(httpGet(uri));
    }

    protected HttpEntity httpPost(String uri, String data) throws Exception {
        return httpPost(uri, data, 201);
    }

    protected HttpEntity httpPost(String uri, String data, int statusCode) throws Exception {
        HttpPost post = new HttpPost(uri);
        post.addHeader(HttpHeaders.Names.ACCEPT, MediaType.JSON.toString());
        post.addHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JSON.toString());
        post.setEntity(new StringEntity(data, ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
            HttpEntity entity = response.getEntity();
            assertThat(entity).isNotNull();
            return entity;
        }
    }

    protected String createResource(String uri, String data) throws Exception {
        return EntityUtils.toString(httpPost(uri, data));
    }

    protected JsonNode createResource(String uri, JsonNode data) throws Exception {
        return toJSON(httpPost(uri, data.toString()));
    }

    protected HttpEntity httpPut(String uri, String data) throws Exception {
        return httpPut(uri, data, 200);
    }

    protected HttpEntity httpPut(String uri, String data, int statusCode) throws Exception {
        HttpPut put = new HttpPut(uri);
        put.addHeader(HttpHeaders.Names.ACCEPT, MediaType.JSON.toString());
        put.addHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JSON.toString());
        put.setEntity(new StringEntity(data, ContentType.create(MediaType.JSON.toString(), "UTF-8")));

        try (CloseableHttpResponse response = httpClient.execute(put)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
            HttpEntity entity = response.getEntity();
            assertThat(entity).isNotNull();
            return entity;
        }
    }

    protected String updateResource(String uri, String data) throws Exception {
        return EntityUtils.toString(httpPut(uri, data));
    }

    protected JsonNode updateResource(String uri, JsonNode data) throws Exception {
        return toJSON(httpPut(uri, data.toString()));
    }

    protected HttpEntity httpDelete(String uri) throws Exception {
        return httpDelete(uri, 200);
    }

    protected HttpEntity httpDelete(String uri, int statusCode) throws Exception {
        HttpDelete delete = new HttpDelete(uri);
        delete.addHeader(HttpHeaders.Names.ACCEPT, MediaType.JSON.toString());

        try (CloseableHttpResponse response = httpClient.execute(delete)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusLine().getStatusCode()).isEqualTo(statusCode);
            HttpEntity entity = response.getEntity();
            assertThat(entity).isNotNull();
            return entity;
        }
    }

    protected String deleteResourceAsString(String uri) throws Exception {
        return EntityUtils.toString(httpDelete(uri));
    }

    protected JsonNode deleteResourceAsJson(String uri) throws Exception {
        return toJSON(httpDelete(uri));
    }

    @Before
    public void setUpSystem() throws Exception {
        try {
            this.system = LiveOakFactory.create();
            this.client = this.system.client();
            this.vertx = this.system.vertx();

            // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
            this.system.awaitStability();

            this.system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
            this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application", applicationDirectory());

            loadExtensions();

            this.system.awaitStability();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @After
    public void tearDownSystem() throws Exception {
        removeAllResources();

        this.system.awaitStability();
        this.system.stop();
    }

    protected void removeAllResources() throws InterruptedException {
        for (InternalApplicationExtension extension : this.extensions) {
            extension.remove();
        }

        this.extensions.clear();
        this.application.configurationFile().delete();

        this.system.awaitStability();
    }

    @Before
    public void setUpClient() throws Exception {
        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        this.httpClient = HttpClients.custom().disableContentCompression().setDefaultRequestConfig(cconfig).build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }
}
