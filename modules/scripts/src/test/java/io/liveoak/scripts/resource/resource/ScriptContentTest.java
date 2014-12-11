package io.liveoak.scripts.resource.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.MediaType;
import org.apache.http.HttpResponse;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ScriptContentTest extends BaseResourceTriggeredTestCase {

    @Test
    public void scriptResource() throws Exception {
        // Test #1 - No Script present after resource creation
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"noscript\", \"target-path\": \"/testApp\" }")).hasStatus(201);

        assertThat(execGet(RESOURCE_SCRIPT_PATH + "/noscript/script")).hasStatus(404);

        // Test #2 - Create Resource and Script
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"create\", \"target-path\": \"/testApp\" }")).hasStatus(201);

        String content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/create").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        // Test #3 - Check Files
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"filecheck\", \"target-path\": \"/testApp\" }")).hasStatus(201);

        File resourceBasedDir = new File(scriptDirectory, "resource-based");
        assertThat(resourceBasedDir.exists()).isTrue();

        File fileCheckDir = new File(resourceBasedDir, "filecheck");
        assertThat(fileCheckDir.exists()).isTrue();
        assertThat(fileCheckDir.listFiles().length).isEqualTo(1);
        assertThat(fileCheckDir.listFiles()[0].getName()).isEqualTo("metadata.json");
        File metadata = new File(fileCheckDir, "metadata.json");
        BufferedReader metadataReader = new BufferedReader(new FileReader(metadata));
        metadataReader.readLine();
        assertThat(metadataReader.readLine()).isEqualTo("  \"target-path\" : \"/testApp\"");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/filecheck").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        assertThat(fileCheckDir.listFiles().length).isEqualTo(2);
        File source = new File(fileCheckDir, "source.js");
        assertThat(source.exists()).isTrue();
        assertThat(source.length()).isGreaterThan(0);
        BufferedReader reader = new BufferedReader(new FileReader(source));
        assertThat(reader.readLine()).isEqualTo(content);

        // Test #4 - Delete Script Fails
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"deletefailhttp\", \"target-path\": \"/testApp\" }")).hasStatus(201);

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/deletefailhttp").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/deletefailhttp/script")).hasStatus(405);

        // Test #5 - Check Script deleted when Resource deleted
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"delete\", \"target-path\": \"/testApp\" }")).hasStatus(201);

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
        execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"media\", \"target-path\": \"/testApp\" }");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/media").data(content, MediaType.JAVASCRIPT).execute()).hasStatus(406);

        // Test #7 - Create and Update Script
        execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"update\", \"target-path\": \"/testApp\" }");
        assertThat(put(RESOURCE_SCRIPT_PATH + "/update").data("{ \"id\": \"update\", \"target-path\": \"/testApp/*\" }").execute()).hasStatus(200);
        metadata = new File(new File(resourceBasedDir, "update"), "metadata.json");
        metadataReader = new BufferedReader(new FileReader(metadata));
        metadataReader.readLine();
        assertThat(metadataReader.readLine()).isEqualTo("  \"target-path\" : \"/testApp/*\"");

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/update").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        source = new File(new File(resourceBasedDir, "update"), "source.js");
        reader = new BufferedReader(new FileReader(source));
        assertThat(reader.readLine()).isEqualTo(content);

        content = "function postRead(response, libraries) { print('Goodbye');}";
        assertThat(put(RESOURCE_SCRIPT_PATH + "/update/script").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(200);
        assertThat(httpResponse.getEntity()).matches(content);
        reader = new BufferedReader(new FileReader(source));
        assertThat(reader.readLine()).isEqualTo(content);

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

        // Test #8 - check that the script is not overwritten when the metadata is updated
        testUpdateDataResource();
    }

    // Test that a script is not overwritten when the metadata is updated
    public void testUpdateDataResource() throws Exception {
        String id = "checkDataUpdate";
        HttpResponse response =  execPost(RESOURCE_SCRIPT_PATH, "{ 'id': '" + id +"', 'target-path': '/testApp' }");
        assertThat(response).hasStatus(201);
        String content = "function preRead(request, libraries) { print('Hello');}";
        HttpResponse scriptResponse = post(RESOURCE_SCRIPT_PATH + "/" + id).accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute();
        assertThat(scriptResponse).hasStatus(201);
        assertThat(scriptResponse.getEntity()).matches(content);

        ObjectNode jsonData = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);
        assertThat(jsonData.get("target-path").textValue()).isEqualTo("/testApp");
        assertThat(jsonData.get("provides").size()).isEqualTo(1);
        assertThat(jsonData.get("provides").get(0).textValue()).isEqualTo("PREREAD");
        assertThat(jsonData.get("enabled").booleanValue()).isEqualTo(true);

        // Update the metadata
        execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id +"', 'target-path': '/testApp', 'enabled': false }");
        assertThat(response).hasStatus(201);

        ObjectNode updatedData = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);
        assertThat(updatedData.get("target-path").textValue()).isEqualTo("/testApp");
        assertThat(updatedData.get("provides").size()).isEqualTo(1);
        assertThat(updatedData.get("provides").get(0).textValue()).isEqualTo("PREREAD");
        assertThat(updatedData.get("enabled").booleanValue()).isEqualTo(false);

        HttpResponse scriptReadResponse = get(RESOURCE_SCRIPT_PATH + "/" + id + "/script").accept(MediaType.JAVASCRIPT).execute();
        assertThat(scriptReadResponse).hasStatus(200);
        assertThat(scriptReadResponse.getEntity()).matches(content);
    }
}
