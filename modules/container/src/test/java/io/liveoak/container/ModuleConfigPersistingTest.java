package io.liveoak.container;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.exceptions.DeleteNotSupportedException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ModuleConfigPersistingTest extends AbstractContainerTest {

    private static Client client;

    private static ObjectMapper mapper;

    private static final String CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system/mock";

    private static File CONFIG_FILE;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream inputStream = ModuleConfigPersistingTest.class.getClassLoader().getResourceAsStream("mock.json");

        File dir = File.createTempFile("test", null);
        dir.delete(); dir.mkdir();
        File file = new File(dir, "mock.json");
        FileOutputStream outputStream = new FileOutputStream(file);

        int read = inputStream.read();
        while (read >= 0) {
            outputStream.write(read);
            read = inputStream.read();
        }

        inputStream.close();
        outputStream.close();
        CONFIG_FILE = file;

        system = LiveOakFactory.create();
        client = system.client();

        system.extensionInstaller().load(new MockExtension(), CONFIG_FILE);

        awaitStability();

        system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);

        awaitStability();

        mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @AfterClass
    public static void shutdown() {
        system.stop();
    }

    @Test
    public void testReadResource() throws Exception {
        ResourceState configResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH);
        assertThat(configResourceState).isNotNull();
        assertThat(configResourceState.members().size()).isEqualTo(3);

        ResourceState moduleResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/module");
        assertThat(moduleResourceState).isNotNull();
        assertThat(moduleResourceState.id()).isEqualTo("module");
        assertThat(moduleResourceState.getProperty("hello")).isEqualTo("mock");
        assertThat(moduleResourceState.getProperty("foo")).isEqualTo("bar");

        ResourceState foo = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/foo");
        assertThat(foo).isNotNull();
        assertThat(foo.id()).isEqualTo("foo");
        assertThat(foo.getProperty("hello")).isEqualTo("foo");

        ResourceState bar = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/bar");
        assertThat(bar).isNotNull();
        assertThat(bar.id()).isEqualTo("bar");
        assertThat(bar.getProperty("hello")).isEqualTo("bar");
    }

    @Test
    public void testUpdateModule() throws Exception {
        ResourceState moduleResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/module");
        assertThat(moduleResourceState.getProperty("hello")).isEqualTo("mock");
        assertThat(moduleResourceState.getProperty("foo")).isEqualTo("bar");

        moduleResourceState.putProperty("foo", "baz");
        moduleResourceState.removeProperty("hello");
        moduleResourceState.putProperty("hi", "mock");

        ResourceState updatedResourceState = client.update(new RequestContext.Builder().build(), CONFIG_PATH + "/module", moduleResourceState);
        // check the result we get back from the update
        assertThat(updatedResourceState.getProperty("hi")).isEqualTo("mock");
        assertThat(updatedResourceState.getProperty("foo")).isEqualTo("baz");
        assertThat(updatedResourceState.getPropertyNames().contains("hello")).isFalse();

        ResourceState readResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/module");
        // check that what we read is updated
        assertThat(readResourceState.getProperty("hi")).isEqualTo("mock");
        assertThat(readResourceState.getProperty("foo")).isEqualTo("baz");
        assertThat(readResourceState.getPropertyNames().contains("hello")).isFalse();

        // check the file now.
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);
        assertThat(fullConfig.get("instances")).isNotEmpty();
        ObjectNode moduleConfig = (ObjectNode)fullConfig.get("config");
        assertThat(moduleConfig.get("hi").textValue()).isEqualTo("mock");
        assertThat(moduleConfig.get("foo").textValue()).isEqualTo("baz");
        assertThat(moduleConfig.has("hello")).isFalse();
    }

    @Test
    public void testUpdateInstance() throws Exception {
        ResourceState fooResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/foo");
        assertThat(fooResourceState.getProperty("hello")).isEqualTo("foo");

        fooResourceState.putProperty("hello", "foot");
        fooResourceState.putProperty("good", "bye");

        ResourceState updatedResourceState = client.update(new RequestContext.Builder().build(), CONFIG_PATH + "/foo", fooResourceState);
        assertThat(updatedResourceState.getProperty("hello")).isEqualTo("foot");
        assertThat(updatedResourceState.getProperty("good")).isEqualTo("bye");

        ResourceState readResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/foo");
        assertThat(readResourceState.getProperty("hello")).isEqualTo("foot");
        assertThat(readResourceState.getProperty("good")).isEqualTo("bye");

        //check the file now
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);
        ObjectNode foo = (ObjectNode)((ObjectNode)fullConfig.get("instances")).get("foo");
        assertThat(foo.get("hello").textValue()).isEqualTo("foot");
        assertThat(foo.get("good").textValue()).isEqualTo("bye");
    }

    @Test
    public void testDeleteInstance() throws Exception {
        ResourceState barResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/bar");
        assertThat(barResourceState).isNotNull();
        assertThat(barResourceState.getProperty("hello")).isEqualTo("bar");

        ResourceState deleteResourceState = client.delete(new RequestContext.Builder().build(), CONFIG_PATH + "/bar");
        assertThat(deleteResourceState).isNotNull();
        assertThat(deleteResourceState.getProperty("hello")).isEqualTo("bar");

        try {
            client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/bar");
            fail();
        } catch (ResourceNotFoundException e) {
            //expected
        }

        //check the file now
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);
        ObjectNode bar = (ObjectNode)((ObjectNode)fullConfig.get("instances")).get("bar");
        assertThat(bar).isNull();
        ObjectNode foo = (ObjectNode)((ObjectNode)fullConfig.get("instances")).get("foo");
        assertThat(foo).isNotEmpty();
        ObjectNode moduleConfig = (ObjectNode)fullConfig.get("config");
        assertThat(moduleConfig).isNotEmpty();
    }

    @Test
    public void testDeleteModule() throws Exception {
        ObjectNode originalConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);

        ResourceState moduleResourceState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/module");
        assertThat(moduleResourceState).isNotNull();

        try {
            client.delete(new RequestContext.Builder().build(),CONFIG_PATH + "/module");
            fail();
        } catch (DeleteNotSupportedException e) {
            // expected
        }

        //check the file
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);
        assertThat(fullConfig.get("config")).isNotEmpty();
        assertThat(fullConfig.asText()).isEqualTo(originalConfig.asText());
    }

    @Test
    public void testCreateInstance() throws Exception {
        try {
            client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/baz");
            fail();
        } catch (ResourceNotFoundException e) {
            //expected
        }

        ResourceState bazResourceState = new DefaultResourceState("baz");
        bazResourceState.putProperty("hello", "baz");

        ResourceState createdState = client.create(new RequestContext.Builder().build(), CONFIG_PATH, bazResourceState);
        assertThat(createdState).isNotNull();
        assertThat(createdState.id()).isEqualTo("baz");
        assertThat(createdState.getProperty("hello")).isEqualTo("baz");

        //Check that what we get back on a read is correct
        ResourceState readState = client.read(new RequestContext.Builder().build(), CONFIG_PATH + "/baz");
        assertThat(readState).isNotNull();
        assertThat(readState.getProperty("hello")).isEqualTo("baz");

        //Check the file
        ObjectNode fullConfig = (ObjectNode) mapper.readTree(CONFIG_FILE);
        assertThat(fullConfig.get("config")).isNotEmpty();
        ObjectNode baz =  (ObjectNode)((ObjectNode)fullConfig.get("instances")).get("baz");
        assertThat(baz).isNotEmpty();
        assertThat(baz.get("hello").textValue()).isEqualTo("baz");


    }
}
