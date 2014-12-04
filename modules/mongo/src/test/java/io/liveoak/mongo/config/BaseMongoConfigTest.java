package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class BaseMongoConfigTest extends AbstractTestCaseWithTestApp {

    static String BASEPATH = "storage";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;
    static final String SYSTEM_CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system/mongo/module";
    static final String INSTANCES_CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system/mongo";

    static final String RUNNING_MONGO_HOST = System.getProperty("mongo.host", "localhost");
    static final Integer RUNNING_MONGO_PORT = new Integer(System.getProperty("mongo.port", "27017"));


    @BeforeClass
    public static void loadExtensions() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree(
                "{ name: 'testDefaultDB'," +
                " servers: [{" +
                        "host:'" + RUNNING_MONGO_HOST + "'," +
                        "port:" + RUNNING_MONGO_PORT +
                        "}]}");

        loadExtension("mongo", new MongoExtension(), (ObjectNode) configNode);
    }

    @After
    public void postTestCleanup() throws InterruptedException {
        awaitStability();
        removeAllResources();
    }

    protected InternalApplicationExtension setUpSystem(ResourceState config) throws Exception {
        return installTestAppResource("mongo", BASEPATH, config);
    }

    protected InternalApplicationExtension setUpSystem(ObjectNode objectNode) throws Exception {
        return installTestAppResource("mongo", BASEPATH, objectNode);
    }

    protected InternalApplicationExtension setUpSystem(String databaseName, String host, Integer port) throws Exception {
        JsonNode configNode =  ObjectMapperFactory.create().readTree(
                "{ db: '" + databaseName + "'," +
                 " servers: [{" +
                        "host:'" + RUNNING_MONGO_HOST + "'," +
                        "port:" + RUNNING_MONGO_PORT +
                        "}]}");

        return installTestAppResource("mongo", BASEPATH, (ObjectNode)configNode);
    }

    protected ResourceState createConfig(String databaseName, String host, Integer port) throws Exception {
        ResourceState config = new DefaultResourceState();
        config = new DefaultResourceState();
        config.putProperty("db", databaseName);

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", host);
        server.putProperty("port", port);

        List<ResourceState> resourceStates = new ArrayList<ResourceState>();
        resourceStates.add(server);

        config.putProperty("servers", resourceStates);

        return config;
    }
}
