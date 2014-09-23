package io.liveoak.scripts.resource;

import java.util.List;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ProvidesPropertyTest extends BaseResourceTriggeredTestCase {

    @Test
    public void checkSingleProvides() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("single", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/single");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/single");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0)).isEqualTo("PREREAD");
    }

    @Test
    public void checkMultipleProvides() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("multiple", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/multiple");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');} function postRead(response, libraries) { print('Goodbye');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/multiple");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(2);
        assertThat(provides.contains("PREREAD")).isTrue();
        assertThat(provides.contains("POSTREAD")).isTrue();
    }

    @Test
    public void checkProvidesAfterScriptUpdateWithDifferentMethods() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("update", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/update");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/update");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0)).isEqualTo("PREREAD");

        HttpPut put = new HttpPut("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/update/script");
        put.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        put.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        content = "function postRead(response, libraries) { print('Goodbye');}";
        entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        put.setEntity(entity);

        response = httpClient.execute(put);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/update");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0)).isEqualTo("POSTREAD");
    }

    @Test
    public void checkInvalidMethodsAreNotCounted() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("count", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/count");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preReads(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/count");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(0);
    }

    @Test
    public void checkLowerCaseMethodsAreNotCounted() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("case", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/case");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preread(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/case");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(0);
    }
}
