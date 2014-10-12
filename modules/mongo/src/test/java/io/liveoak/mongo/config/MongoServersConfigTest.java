package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplicationExtension;
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
    public void serverConfigTests() throws Exception {
        // TEST #1 - Empty config
        ResourceState config = new DefaultResourceState();
        InternalApplicationExtension resource = null;

        //should fail since the config needs at least a DB property specified
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
            this.system.awaitStability();
        }


        // TEST #2 - Invalid db config
        config = new DefaultResourceState();
        config.putProperty("db", 123);

        //should fail since the DB property should be a string
        try {
            resource = setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
            this.system.awaitStability();
        }


        // TEST #3 - Null db config
        config = new DefaultResourceState();
        config.putProperty("db", null);

        //should fail since the DB propery should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
            this.system.awaitStability();
        }


        // TEST #4 - Empty db config
        config = new DefaultResourceState();
        config.putProperty("db", "");

        //should fail since the DB propery should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
            this.system.awaitStability();
        }


        // TEST #5 - Embedded resources
        config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
        resource = setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();
        assertThat(result.getProperty(ReadPreferenceState.ID)).isNotNull();
        assertThat(result.getProperty(MongoClientOptionsState.ID)).isNotNull();

        // Reset for next test
        removeResource(resource);


        // TEST #6 - Only db
        config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testOnlyDBDatabase");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #7 - Single server host only
        config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        List<ResourceState> serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("123.123.123.123");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #8 - Single server
        config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", 12345);
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("123.123.123.123");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(12345);

        // Reset for next test
        removeResource(resource);


        // TEST #9 - Empty server
        config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        server = new DefaultResourceState();
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017);

        // Reset for next test
        removeResource(resource);


        // TEST #10 - Same server twice
        //Note: the mongo client does support this for some reason, so it will work without error.
        config = new DefaultResourceState();
        config.putProperty("db", "testSameServerTwiceDB");

        servers = new ArrayList<>();

        ResourceState serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        serverA.putProperty("port", 27018);
        servers.add(serverA);

        ResourceState serverB = new DefaultResourceState();
        serverB.putProperty("host", "localhost");
        serverB.putProperty("port", 27018);
        servers.add(serverB);

        config.putProperty("servers", servers);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(),  ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testSameServerTwiceDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> serversResult = (List) result.getProperty("servers");
        assertThat(serversResult.size()).isEqualTo(2);
        assertThat(serversResult.get(0).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(0).getProperty("port")).isEqualTo(27018);
        assertThat(serversResult.get(1).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(1).getProperty("port")).isEqualTo(27018);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        // Reset for next test
        removeResource(resource);


        // TEST #11 - Servers
        config = new DefaultResourceState();
        config.putProperty("db", "testServersDB");

        servers = new ArrayList<>();

        serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        servers.add(serverA);

        serverB = new DefaultResourceState();
        serverB.putProperty("host", "127.0.0.2");
        serverB.putProperty("port", 65535); // max port number that is valid
        servers.add(serverB);

        servers.add(new DefaultResourceState()); //empty value, should use the default values in this case

        config.putProperty("servers", servers);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        serversResourceState = (List) result.getProperty("servers");
        assertThat(serversResourceState.size()).isEqualTo(3);
        assertThat(serversResourceState.get(0).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResourceState.get(0).getProperty("port")).isEqualTo(27017); //the default port
        assertThat(serversResourceState.get(1).getProperty("host")).isEqualTo("127.0.0.2");
        assertThat(serversResourceState.get(1).getProperty("port")).isEqualTo(65535);
        assertThat(serversResourceState.get(2).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(serversResourceState.get(2).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #12 - Invalid host
        config = new DefaultResourceState();
        config.putProperty("db", "testInvalidHostDB");

        server = new DefaultResourceState();
        server.putProperty("host", "myServer123ABC");
        server.putProperty("port", 12345);
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }

        // Reset for next test
        removeResource(resource);


        // TEST #13 - Empty host
        config = new DefaultResourceState();
        config.putProperty("db", "testEmptyHostDB");

        server = new DefaultResourceState();
        server.putProperty("host", "");
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testEmptyHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #14 - Null host
        config = new DefaultResourceState();

        config.putProperty("db", "testNullHostDB");

        server = new DefaultResourceState();
        server.putProperty("host", null);
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testNullHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #15 - Invalid port
        config = new DefaultResourceState();

        config.putProperty("db", "testSingleServerHostOnlyDB");

        server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", "abc");
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
            this.system.awaitStability();
        }


        // TEST #16 - Null port
        config = new DefaultResourceState();

        config.putProperty("db", "testNullPortDB");

        server = new DefaultResourceState();
        server.putProperty("host", "127.0.0.1");
        server.putProperty("port", null);
        serversResourceState = new ArrayList<>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testNullPortDB");
        assertThat(result.getProperty("servers")).isNotNull();

        servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port

        // Reset for next test
        removeResource(resource);


        // TEST #17 - Update servers
        config = new DefaultResourceState();
        config.putProperty("db", "testUpdateServersDB");

        servers = new ArrayList<>();

        serverA = new DefaultResourceState();
        serverA.putProperty("host", "localhost");
        servers.add(serverA);

        serverB = new DefaultResourceState();
        serverB.putProperty("host", "127.0.0.2");
        serverB.putProperty("port", 65535); // max port number that is valid
        servers.add(serverB);

        servers.add(new DefaultResourceState()); //empty value, should use the default values in this case
        config.putProperty("servers", servers);

        resource = setUpSystem(config);

        ResourceState updatedConfig = new DefaultResourceState();
        List<ResourceState> updatedServers = new ArrayList<ResourceState>();
        updatedConfig.putProperty("db", "testUpdateServersDB");

        ResourceState updatedServerB = new DefaultResourceState();
        updatedServerB.putProperty("host", "127.0.0.3");
        updatedServerB.putProperty("port", 12345); // max port number that is valid
        updatedServers.add(updatedServerB);

        updatedConfig.putProperty("servers", updatedServers);

        result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        serversResourceState = (List) result.getProperty("servers");
        assertThat(serversResourceState.size()).isEqualTo(1);
        assertThat(serversResourceState.get(0).getProperty("host")).isEqualTo("127.0.0.3");
        assertThat(serversResourceState.get(0).getProperty("port")).isEqualTo(12345);

        // Reset for next test
        removeResource(resource);


        // TEST #18 - Modify configuration
        config = new DefaultResourceState();
        config.putProperty("db", "testModifyConfiguration");
        setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        client.update(new RequestContext.Builder().build(), ADMIN_PATH, result);
    }

}
