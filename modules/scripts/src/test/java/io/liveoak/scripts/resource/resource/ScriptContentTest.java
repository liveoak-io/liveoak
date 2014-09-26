package io.liveoak.scripts.resource.resource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.MediaType;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.HttpPut;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ScriptContentTest extends BaseResourceTriggeredTestCase {

    @Test
    public void scriptResource() throws Exception {
        // Test #1 - No Script present after resource creation
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"noscript\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        assertThat(execGet(RESOURCE_SCRIPT_PATH + "/noscript/script")).hasStatus(404);

        // Test #2 - Create Resource and Script
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"create\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        String content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/create").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        // Test #3 - Check Files
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"filecheck\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        File resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File fileCheckDir = new File(resourceBasedDir, "filecheck");
        assertThat(fileCheckDir.exists()).isTrue();
        assertThat(fileCheckDir.listFiles().length).isEqualTo(1);
        assertThat(fileCheckDir.listFiles()[0].getName()).isEqualTo("metadata.json");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/filecheck").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        assertThat(fileCheckDir.listFiles().length).isEqualTo(2);
        File source = new File(fileCheckDir, "source.js");
        assertThat(source.exists()).isTrue();
        assertThat(source.length()).isGreaterThan(0);

        // Test #4 - Delete Script Fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"deletefailhttp\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/deletefailhttp").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/deletefailhttp/script")).hasStatus(405);

        // Test #5 - Check Script deleted when Resource deleted
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"delete\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/delete").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        execDelete(RESOURCE_SCRIPT_PATH + "/delete");

        resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File deleteDir = new File(resourceBasedDir, "delete");
        assertThat(deleteDir.exists()).isFalse();

        assertThat(get(RESOURCE_SCRIPT_PATH + "/delete/script").accept(MediaType.JAVASCRIPT).execute()).hasStatus(404);

        // Test #6 - Fail with invalid media type
        execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"media\", \"target-path\": \"targetPath\" }");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/media").data(content, MediaType.JAVASCRIPT).execute()).hasStatus(406);

        // Test #7 - Create and Update Script
        execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"update\", \"target-path\": \"targetPath\" }");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/update").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        content = "function postRead(response, libraries) { print('Goodbye');}";
        assertThat(put(RESOURCE_SCRIPT_PATH + "/update/script").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(200);
        assertThat(httpResponse.getEntity()).matches(content);

        resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        deleteDir = new File(resourceBasedDir, "update");
        assertThat(deleteDir.exists()).isTrue();

        assertThat(deleteDir.listFiles().length).isEqualTo(2);
        source = new File(deleteDir, "source.js");
        assertThat(source.exists()).isTrue();
        assertThat(source.length()).isGreaterThan(0);
        String fileContent = new String(Files.readAllBytes(Paths.get(source.toURI())));
        assertThat(fileContent).isEqualTo(content);
    }
}
