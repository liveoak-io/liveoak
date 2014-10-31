package io.liveoak.mongo.config;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @BeforeClass
    public static void loadExtensions() throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("db", "testDefaultDB");
        loadExtension("mongo", new MongoExtension(), json);
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
}
