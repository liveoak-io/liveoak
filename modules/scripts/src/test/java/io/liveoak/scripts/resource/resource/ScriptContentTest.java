package io.liveoak.scripts.resource.resource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.DeleteNotSupportedException;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ScriptContentTest extends BaseResourceTriggeredTestCase {

    @Test
    public void noScriptOnResourceCreate() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("noscript", "targetPath").build());
        assertThat(result).isNotNull();

        HttpGet get = new HttpGet("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/noscript/script");
        get.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        CloseableHttpResponse response = httpClient.execute(get);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);
        response.close();
    }

    @Test
    public void createScript() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("create", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/create");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();
    }

    @Test
    public void scriptFilePresentAfterCreate() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JSON.toString());
        post.setEntity(new StringEntity("{ \"id\": \"filecheck\", \"target-path\": \"targetPath\" }"));

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        response.close();

        File resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File fileCheckDir = new File(resourceBasedDir, "filecheck");
        assertThat(fileCheckDir.exists()).isTrue();
        assertThat(fileCheckDir.listFiles().length).isEqualTo(1);
        assertThat(fileCheckDir.listFiles()[0].getName()).isEqualTo("metadata.json");

        post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/filecheck");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        assertThat(fileCheckDir.listFiles().length).isEqualTo(2);
        File source = new File(fileCheckDir, "source.js");
        assertThat(source.exists()).isTrue();
        assertThat(source.length()).isGreaterThan(0);
    }

    @Test
    public void deleteScriptFailsHttp() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JSON.toString());
        post.setEntity(new StringEntity("{ \"id\": \"deletefailhttp\", \"target-path\": \"targetPath\" }"));

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        response.close();

        post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/deletefailhttp");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        HttpDelete delete = new HttpDelete("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/deletefailhttp/script");

        response = httpClient.execute(delete);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(405);
        response.close();
    }

    @Test(expected = DeleteNotSupportedException.class)
    public void deleteScriptFails() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("deletefail", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/deletefail");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        client.delete(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/deletefail/script");
    }

    @Test
    public void scriptDeletedOnResourceDelete() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("delete", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/delete");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        post.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        result = client.delete(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/delete");
        assertThat(result).isNotNull();

        File resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File deleteDir = new File(resourceBasedDir, "delete");
        assertThat(deleteDir.exists()).isFalse();

        HttpGet get = new HttpGet("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/delete/script");
        get.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        response = httpClient.execute(get);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);
        response.close();
    }

    @Test
    public void createAndUpdateScript() throws Exception {
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
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        HttpPut put = new HttpPut("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/update/script");
        put.setHeader(HttpHeaders.Names.CONTENT_TYPE, MediaType.JAVASCRIPT.toString());
        put.setHeader(HttpHeaders.Names.ACCEPT, MediaType.JAVASCRIPT.toString());

        content = "function postRead(response, libraries) { print('Goodbye');}";
        entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        put.setEntity(entity);

        response = httpClient.execute(put);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(response.getEntity()).isNotNull();
        assertThat(EntityUtils.toString(response.getEntity())).isEqualTo(content);
        response.close();

        File resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File deleteDir = new File(resourceBasedDir, "update");
        assertThat(deleteDir.exists()).isTrue();

        assertThat(deleteDir.listFiles().length).isEqualTo(2);
        File source = new File(deleteDir, "source.js");
        assertThat(source.exists()).isTrue();
        assertThat(source.length()).isGreaterThan(0);
        String fileContent = new String(Files.readAllBytes(Paths.get(source.toURI())));
        assertThat(fileContent).isEqualTo(content);
    }

    @Test
    public void createFailsWithInvalidMediaType() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("media", "targetPath").build());
        assertThat(result).isNotNull();

        HttpPost post = new HttpPost("http://localhost:8080" + RESOURCE_SCRIPT_PATH + "/media");

        String content = "function preRead(request, libraries) { print('Hello');}";
        StringEntity entity = new StringEntity(content, ContentType.create(MediaType.JAVASCRIPT.toString(), "UTF-8"));
        post.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();
    }
}
