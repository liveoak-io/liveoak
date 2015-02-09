package io.liveoak.mongo.internal;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.mongo.internal.extension.MongoInternalExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.DeleteNotSupportedException;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalConfigTestCase extends AbstractTestCaseWithTestApp {

    static final String SYSTEM_CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system/mongo-internal/module";

    static final String RUNNING_MONGO_HOST = System.getProperty("mongo.host", "localhost");
    static final Integer RUNNING_MONGO_PORT = new Integer(System.getProperty("mongo.port", "27017"));

    ResourceState original;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree(
                "{name:'default'}");

        JsonNode instancesNode = ObjectMapperFactory.create().readTree(
                "{" +
                        "    foo: {name: 'foo', servers: [{ host: 'localhost', port: 27018}]}," +
                        "    bar: {name: 'bar', servers: [{ port: 27017}]}," +
                        "    baz: {name: 'baz'}" +
                        "}");

        loadExtension("mongo", new MongoExtension(), (ObjectNode) configNode, (ObjectNode) instancesNode);

        JsonNode internalConfig = ObjectMapperFactory.create().readTree(
                "{  db: 'internal'," +
                        " servers: [{host: '" + RUNNING_MONGO_HOST + "', port: " + RUNNING_MONGO_PORT  +  "}]" +
                        "}"
        );
        loadExtension("mongo-internal", new MongoInternalExtension(), (ObjectNode)internalConfig);
    }

    @Before
    public void setup() throws Exception {
        original =  client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
    }

    @After
    public void reset() throws Exception {
        client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, original);
    }

    @Test
    public void readConfigTest() throws Exception {
        ResourceState systemConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        assertThat(systemConfigState).isNotNull();
        assertThat(systemConfigState.getProperty("db")).isEqualTo("internal");

        ResourceState server = (ResourceState)systemConfigState.getProperty("servers", true, List.class).get(0);
        assertThat(server.getProperty("host")).isEqualTo(RUNNING_MONGO_HOST);
        assertThat(server.getProperty("port")).isEqualTo(RUNNING_MONGO_PORT);
    }

    @Test
    public void editConfigTest() throws Exception {
        ResourceState systemConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        assertThat(systemConfigState).isNotNull();
        assertThat(systemConfigState.getProperty("db")).isEqualTo("internal");

        systemConfigState.putProperty("db", "somethingElse");
        ResourceState updateResponse = client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, systemConfigState);
        assertThat(updateResponse.getProperty("db")).isEqualTo("somethingElse");

        ResourceState updatedRead = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        assertThat(updatedRead.getProperty("db")).isEqualTo("somethingElse");
    }

    @Test
    public void deleteConfigTest() throws Exception {
        try {
            client.delete(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
            fail();
        } catch (DeleteNotSupportedException e) {
            //expected
        }
    }


}
