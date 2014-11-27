package io.liveoak.mongo.config;

import java.util.ArrayList;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigClientOptionsTest extends BaseMongoConfigTest {

    @Test
    public void defaultTest() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        config.putProperty("servers", new ArrayList());
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsState.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsState.ID);
        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.DESCRIPTION)).isEqualTo("liveoak");
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECTIONS_PER_HOST)).isEqualTo(100);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER)).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_WAIT_TIME)).isEqualTo(120000);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECT_TIMEOUT)).isEqualTo(10000);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.AUTOCONNECT_RETRY)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_AUTOCONNECT_RETRY_TIME)).isEqualTo(new Long(0));
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CURSOR_FINALIZER_ENABLED)).isEqualTo(true);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.ALWAYS_USE_MBEANS)).isEqualTo(false);
    }

    @Test
    public void configure() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureDB");
        config.putProperty("servers", new ArrayList());

        ResourceState mongoClientConfigResourceState = new DefaultResourceState();
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.DESCRIPTION, "my cool mbaas");
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.MAX_AUTOCONNECT_RETRY_TIME, new Long(50));
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE, true);

        config.putProperty(MongoClientOptionsState.ID, mongoClientConfigResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testConfigureDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsState.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsState.ID);
        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECTIONS_PER_HOST)).isEqualTo(100);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER)).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_WAIT_TIME)).isEqualTo(120000);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECT_TIMEOUT)).isEqualTo(10000);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.AUTOCONNECT_RETRY)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CURSOR_FINALIZER_ENABLED)).isEqualTo(true);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.ALWAYS_USE_MBEANS)).isEqualTo(false);

        // the modified values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.DESCRIPTION)).isEqualTo("my cool mbaas");
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_AUTOCONNECT_RETRY_TIME)).isEqualTo(new Long(50));
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE)).isEqualTo(true);
    }

    @Test
    public void updateConfigure() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateConfigureDB");
        config.putProperty("servers", new ArrayList());

        ResourceState mongoClientConfigResourceState = new DefaultResourceState();
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.DESCRIPTION, "my cool mbaas");
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.MAX_AUTOCONNECT_RETRY_TIME, 50); //test that specifying an int here works
        mongoClientConfigResourceState.putProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE, true);

        config.putProperty(MongoClientOptionsState.ID, mongoClientConfigResourceState);

        setUpSystem(config);

        ResourceState updatedClientConfigResourceState = new DefaultResourceState();
        updatedClientConfigResourceState.putProperty(MongoClientOptionsState.DESCRIPTION, "the awesome mbaas");
        updatedClientConfigResourceState.putProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE, false);
        updatedClientConfigResourceState.putProperty(MongoClientOptionsState.MAX_WAIT_TIME, 1000);

        ResourceState updatedConfig = new DefaultResourceState();
        updatedConfig.putProperty("db", "testUpdateConfigureDB");
        updatedConfig.putProperty("servers", new ArrayList());
        updatedConfig.putProperty(MongoClientOptionsState.ID, updatedClientConfigResourceState);

        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        assertThat(result.getProperty("db")).isEqualTo("testUpdateConfigureDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsState.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsState.ID);

        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECTIONS_PER_HOST)).isEqualTo(100);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER)).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CONNECT_TIMEOUT)).isEqualTo(10000);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.AUTOCONNECT_RETRY)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.CURSOR_FINALIZER_ENABLED)).isEqualTo(true);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.ALWAYS_USE_MBEANS)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_AUTOCONNECT_RETRY_TIME)).isEqualTo(new Long(0));

        // the updated values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.DESCRIPTION)).isEqualTo("the awesome mbaas");
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.SOCKET_KEEP_ALIVE)).isEqualTo(false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsState.MAX_WAIT_TIME)).isEqualTo(1000);
    }
}
