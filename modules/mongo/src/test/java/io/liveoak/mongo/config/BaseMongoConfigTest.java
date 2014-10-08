package io.liveoak.mongo.config;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public abstract class BaseMongoConfigTest extends AbstractResourceTestCase {

    static String BASEPATH = "storage";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;
    static final String SYSTEM_CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system/mongo";

    @Override
    public void loadExtensions() throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();

        ObjectNode serverNode = JsonNodeFactory.instance.objectNode();
        serverNode.put("db", "testDefaultDB");
        json.put("internal-database", serverNode);

        loadExtension("mongo", new MongoExtension(), json);
    }

    protected void setUpSystem(ResourceState config) throws Exception {
        installResource("mongo", BASEPATH, config);
    }

    protected void setUpSystem(ObjectNode objectNode) throws Exception {
        installResource("mongo", BASEPATH, objectNode);
    }
}
