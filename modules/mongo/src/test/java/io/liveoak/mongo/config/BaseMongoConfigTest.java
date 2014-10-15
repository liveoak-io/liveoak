package io.liveoak.mongo.config;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplicationExtension;
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
    static final String INSTANCES_CONFIG_PATH = "/" + ZeroExtension.APPLICATION_ID + "/system-instances/mongo";

    @Override
    public void loadExtensions() throws Exception {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("db", "testDefaultDB");
        loadExtension("mongo", new MongoExtension(), json);
    }

    protected InternalApplicationExtension setUpSystem(ResourceState config) throws Exception {
        return installResource("mongo", BASEPATH, config);
    }

    protected InternalApplicationExtension setUpSystem(ObjectNode objectNode) throws Exception {
        return installResource("mongo", BASEPATH, objectNode);
    }
}
