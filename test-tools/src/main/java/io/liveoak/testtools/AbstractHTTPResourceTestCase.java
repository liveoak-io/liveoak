package io.liveoak.testtools;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.spi.MediaType;
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

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class AbstractHTTPResourceTestCase extends AbstractTestCase {

    protected CloseableHttpClient httpClient;
    protected CloseableHttpResponse httpResponse;

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

    public JsonNode toJSON(HttpEntity entity) throws IOException {
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

        public HttpRequest addHeader(String name, String value) {
            if (builder != null) {
                builder.addHeader(name, value);
            }
            return this;
        }

        public HttpRequest setEntity(HttpEntity httpEntity) {
            if (builder != null) {
                builder.setEntity(httpEntity);
            }
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
    public void setUpClient() throws Exception {
        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        this.httpClient = HttpClients.custom()
                .disableContentCompression()
                .setDefaultRequestConfig(cconfig)
                .disableRedirectHandling()
                .build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }
}
