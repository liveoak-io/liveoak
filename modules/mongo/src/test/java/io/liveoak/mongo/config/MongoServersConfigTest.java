package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class MongoServersConfigTest extends BaseMongoConfigTest {

    @Test
    public void emptyConfig() throws Exception {
        ResourceState config = new DefaultResourceState();

        //should fail since the config needs at least a DB property specified
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void invalidDbConfig() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", 123);

        //should fail since the DB property should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void nullDbConfig() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", null);

        //should fail since the DB propery should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void emptyDbConfig() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "");

        //should fail since the DB propery should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void embeddedResources() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
        config.putProperty("servers", new ArrayList());
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();
        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        assertThat(result.getProperty(MongoClientOptionsState.ID)).isNotNull();
    }

    @Test
    public void onlyDb() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
        config.putProperty("servers", new ArrayList());
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testOnlyDBDatabase");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void singleServerHostOnly() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("123.123.123.123");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void singleServer() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", 12345);
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("123.123.123.123");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(12345);
    }

    @Test
    public void emptyServer() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017);
    }

    @Test
    public void sameServerTwice() throws Exception {
        //Note: the mongo client does support this for some reason, so it will work without error.
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSameServerTwiceDB");

        List<ResourceState> servers = new ArrayList<>();

        ResourceState serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        serverA.putProperty("port", 27018);
        servers.add(serverA);

        ResourceState serverB = new DefaultResourceState();
        serverB.putProperty("host", "localhost");
        serverB.putProperty("port", 27018);
        servers.add(serverB);

        config.putProperty("servers", servers);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSameServerTwiceDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> serversResult = (List) result.getProperty("servers");
        assertThat(serversResult.size()).isEqualTo(2);
        assertThat(serversResult.get(0).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(0).getProperty("port")).isEqualTo(27018);
        assertThat(serversResult.get(1).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(1).getProperty("port")).isEqualTo(27018);

        client.read(new RequestContext.Builder().build(), ADMIN_PATH);
    }

    @Test
    public void servers() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testServersDB");

        List<ResourceState> servers = new ArrayList<>();

        ResourceState serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        servers.add(serverA);

        ResourceState serverB = new DefaultResourceState();
        serverB.putProperty("host", "127.0.0.2");
        serverB.putProperty("port", 65535); // max port number that is valid
        servers.add(serverB);

        servers.add(new DefaultResourceState()); //empty value, should use the default values in this case

        config.putProperty("servers", servers);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        List<ResourceState> serversResourceState = (List) result.getProperty("servers");
        assertThat(serversResourceState.size()).isEqualTo(3);
        assertThat(serversResourceState.get(0).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResourceState.get(0).getProperty("port")).isEqualTo(27017); //the default port
        assertThat(serversResourceState.get(1).getProperty("host")).isEqualTo("127.0.0.2");
        assertThat(serversResourceState.get(1).getProperty("port")).isEqualTo(65535);
        assertThat(serversResourceState.get(2).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(serversResourceState.get(2).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void invalidHost() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testInvalidHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "myServer123ABC");
        server.putProperty("port", 12345);
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void emptyHost() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testEmptyHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "");
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testEmptyHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void nullHost() throws Exception {
        ResourceState config = new DefaultResourceState();

        config.putProperty("db", "testNullHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", null);
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testNullHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void invalidPort() throws Exception {
        ResourceState config = new DefaultResourceState();

        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", "abc");
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
        removeAllResources();

        server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", -10);
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);
        config.putProperty("servers", serversResourceState);

        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
        removeAllResources();

        server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", 65536); // 1 above the maximum port number available for use
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);
        config.putProperty("servers", serversResourceState);

        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void nullPort() throws Exception {
        ResourceState config = new DefaultResourceState();

        config.putProperty("db", "testNullPortDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "127.0.0.1");
        server.putProperty("port", null);
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testNullPortDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void updateServers() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateServersDB");

        List<ResourceState> servers = new ArrayList<>();

        ResourceState serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        servers.add(serverA);

        ResourceState serverB = new DefaultResourceState();
        serverB.putProperty("host", "127.0.0.2");
        serverB.putProperty("port", 65535); // max port number that is valid
        servers.add(serverB);

        servers.add(new DefaultResourceState()); //empty value, should use the default values in this case
        config.putProperty("servers", servers);

        setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        List<ResourceState> updatedServers = new ArrayList<ResourceState>();
        updatedConfig.putProperty("db", "testUpdateServersDB");

        ResourceState updatedServerB = new DefaultResourceState();
        updatedServerB.putProperty("host", RUNNING_MONGO_HOST);
        updatedServerB.putProperty("port", RUNNING_MONGO_PORT); // the port the test mongo instance is running on
        updatedServers.add(updatedServerB);

        updatedConfig.putProperty("servers", updatedServers);

        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        List<ResourceState> serversResourceState = (List) result.getProperty("servers");
        assertThat(serversResourceState.size()).isEqualTo(1);
        assertThat(serversResourceState.get(0).getProperty("host")).isEqualTo(RUNNING_MONGO_HOST);
        assertThat(serversResourceState.get(0).getProperty("port")).isEqualTo(RUNNING_MONGO_PORT);
    }

    @Test
    public void modifyConfiguration() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testModifyConfiguration");
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        client.update(new RequestContext.Builder().build(), ADMIN_PATH, result);
    }

}
