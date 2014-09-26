/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
    protected CloseableHttpResponse httpResponse;
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
        fullConfig.put("config", config);
        this.system.extensionInstaller().load(id, ext, fullConfig);
        this.extensionIds.add(id);
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ObjectNode resourceConfig) throws Exception {
        InternalApplicationExtension appExt = this.application.extend(extId, resourceId, resourceConfig);
        this.extensions.add(appExt);
        return appExt;
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ResourceState resourceConfig) throws Exception {
        return installResource(extId, resourceId, ConversionUtils.convert(resourceConfig));
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

    protected JsonNode toJSON(HttpEntity entity) throws IOException {
        return ObjectMapperFactory.create().readTree(entity.getContent());
    }

    protected HttpRequest get(String path) throws Exception {
        return new HttpRequest().get(path);
    }

    protected HttpResponse execGet(String path) throws Exception {
        return get(path).execute();
    }

    protected JsonNode getJSON(String path) throws Exception {
        return toJSON(get(path).execute().getEntity());
    }

    protected HttpRequest post(String path) throws Exception {
        return new HttpRequest().post(path);
    }

    protected HttpResponse execPost(String path, String data) throws Exception {
        return post(path).data(data).execute();
    }

    protected HttpRequest put(String path) throws Exception {
        return new HttpRequest().put(path);
    }

    protected HttpResponse execPut(String path, String data) throws Exception {
        return put(path).data(data).execute();
    }

    protected HttpRequest delete(String path) throws Exception {
        return new HttpRequest().delete(path);
    }

    protected HttpResponse execDelete(String path) throws Exception {
        return delete(path).execute();
    }

    public class HttpRequest {
        RequestBuilder builder;
        final static String HOST = "http://localhost:8080";

        public HttpRequest accept(MediaType mediaType) {
            if (builder != null) {
                builder.addHeader(HttpHeaders.Names.ACCEPT, mediaType.toString());
            }
            return this;
        }

        public HttpRequest data(String data) {
            return data(data, MediaType.JSON);
        }

        public HttpRequest data(String data, MediaType mediaType) {
            if (builder != null) {
                builder.setEntity(new StringEntity(data, ContentType.create(mediaType.toString(), "UTF-8")));
            }
            return this;
        }

        public HttpRequest data(JsonNode data) {
            return data(data, MediaType.JSON);
        }

        public HttpRequest data(JsonNode data, MediaType mediaType) {
            if (builder != null) {
                builder.setEntity(new StringEntity(data.toString(), ContentType.create(mediaType.toString(), "UTF-8")));
            }
            return this;
        }

        public HttpRequest get(String path) {
            builder = RequestBuilder.get().setUri(HOST + path);
            return this;
        }

        public HttpRequest post(String path) {
            builder = RequestBuilder.post().setUri(HOST + path);
            return this;
        }

        public HttpRequest put(String path) {
            builder = RequestBuilder.put().setUri(HOST + path);
            return this;
        }

        public HttpRequest delete(String path) {
            builder = RequestBuilder.delete().setUri(HOST + path);
            return this;
        }

        public HttpResponse execute() throws Exception {
            try (CloseableHttpResponse response = httpClient.execute(builder.build())) {
                assertThat(response).isNotNull();
                httpResponse = response;
                return httpResponse;
            }
        }
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
