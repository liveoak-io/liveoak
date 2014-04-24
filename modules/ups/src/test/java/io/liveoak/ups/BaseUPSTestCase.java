package io.liveoak.ups;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;

import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseUPSTestCase extends AbstractResourceTestCase {

    protected static final String BASEPATH = "push";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;

    @Override
    public void loadExtensions() throws Exception {

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put( "db", "MongoInteralTest_" + UUID.randomUUID());
        loadExtension( "mongo", new MongoExtension(), config );

        loadExtension( "ups", new UPSExtension());
        installResource( "ups", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty( "upsURL", "http://localhost:8080/my_ups_server" );
        config.putProperty( "applicationId", "my-application-id");
        config.putProperty( "masterSecret", "shhhh-its-a-secret");
        return config;
    }
}
