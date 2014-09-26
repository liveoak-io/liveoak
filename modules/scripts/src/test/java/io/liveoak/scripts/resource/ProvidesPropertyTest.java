package io.liveoak.scripts.resource;

import java.util.List;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ProvidesPropertyTest extends BaseResourceTriggeredTestCase {

    @Test
    public void checkSingleProvides() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("single", "targetPath").build());
        assertThat(result).isNotNull();

        String content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/single").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

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

        String content = "function preRead(request, libraries) { print('Hello');} function postRead(response, libraries) { print('Goodbye');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/multiple").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

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

        String content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/update").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/update");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0)).isEqualTo("PREREAD");

        content = "function postRead(response, libraries) { print('Goodbye');}";
        assertThat(put(RESOURCE_SCRIPT_PATH + "/update/script").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(200);

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

        String content = "function preReads(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/count").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

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

        String content = "function preread(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/case").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);

        result = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/case");

        assertThat(result).isNotNull();
        assertThat(result.getProperty("provides")).isNotNull();
        List<String> provides = result.getPropertyAsList("provides");
        assertThat(provides.size()).isEqualTo(0);
    }
}
