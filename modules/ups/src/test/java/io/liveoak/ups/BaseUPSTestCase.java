package io.liveoak.ups;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseUPSTestCase extends AbstractResourceTestCase {

    protected static final String BASEPATH = "push";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "ups", new UPSExtension());
        installResource( "ups", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty( "upsServerURL", "http://localhost:8080/my_ups_server" );
        config.putProperty( "applicationId", "my-application-id");
        config.putProperty( "masterSecret", "shhhh-its-a-secret");
        return config;
    }
}
