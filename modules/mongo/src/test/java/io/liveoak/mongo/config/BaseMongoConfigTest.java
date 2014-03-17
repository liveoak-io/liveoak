package io.liveoak.mongo.config;

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

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("mongo", new MongoExtension());
    }

    protected void setUpSystem(ResourceState config) throws Exception {
        installResource("mongo", BASEPATH, config);
    }


}
