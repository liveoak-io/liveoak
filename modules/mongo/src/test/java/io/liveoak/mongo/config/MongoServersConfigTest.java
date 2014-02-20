package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoServersConfigTest extends BaseMongoConfigTest {



    @Test
    public void testEmptyConfig() throws Exception {
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
    public void testInvalidDBConfig() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", 123);

        //should fail since the DB propery should be a string
        try {
            setUpSystem(config);
            fail();
        } catch (InitializationException e) {
            //expected
        }
    }

    @Test
    public void testNullDBConfig() throws Exception {
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
    public void testEmptyDBConfig() throws Exception {
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
    public void testEmbeddedResources() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();
        //TODO: properly test here

//        assertThat( result.members().get( 0 ).id() ).isEqualTo("MongoClientOptions");
//        assertThat(result.members().get(0).uri()).isEqualTo( new URI("/" + BASEPATH + ";config/MongoClientOptions"));
//        assertThat(result.members().get(0).getPropertyNames().size()).isEqualTo(0);
//
//        assertThat( result.members().get( 1 ).id() ).isEqualTo("WriteConcern");
//        assertThat(result.members().get(1).uri()).isEqualTo( new URI("/" + BASEPATH + ";config/WriteConcern"));
//        assertThat(result.members().get(1).getPropertyNames().size()).isEqualTo(0);
//
//        assertThat(result.members().get(2).id()).isEqualTo("ReadPreferences");
//        assertThat(result.members().get(2).uri()).isEqualTo( new URI("/" + BASEPATH + ";config/ReadPreferences"));
//        assertThat(result.members().get(2).getPropertyNames().size()).isEqualTo(0);
    }

    @Test
    public void testOnlyDB() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testOnlyDBDatabase");
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
    public void testSingleServerHostOnly() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
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
    public void testSingleServer() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", 12345);
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
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
    public void testEmptyServer() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testSingleServerHostOnlyDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017);
    }

    @Test
    public void testSameServerTwice() throws Exception {
        //Note: the mongo client does support this for some reason, so it will work without error.
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSameServerTwiceDB");

        List<ResourceState> servers = new ArrayList<ResourceState>();

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

        ResourceState result = client.read(new RequestContext.Builder().build(),  ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testSameServerTwiceDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> serversResult = (List) result.getProperty("servers");
        assertThat(serversResult.size()).isEqualTo(2);
        assertThat(serversResult.get(0).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(0).getProperty("port")).isEqualTo(27018);
        assertThat(serversResult.get(1).getProperty("host")).isEqualTo("localhost");
        assertThat(serversResult.get(1).getProperty("port")).isEqualTo(27018);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );
    }

    @Test
    public void testServers() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testServersDB");

        List<ResourceState> servers = new ArrayList<ResourceState>();

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

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

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
    public void testInvalidHost() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testInvalidHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "myServer123ABC");
        server.putProperty("port", 12345);
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
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
    public void testEmptyHost() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testEmptyHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "");
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testEmptyHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void testNullHost() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testNullHostDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", null);
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testNullHostDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void testInvalidPort() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testSingleServerHostOnlyDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "123.123.123.123");
        server.putProperty("port", "abc");
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
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
        serversResourceState = new ArrayList<ResourceState>();
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
        serversResourceState = new ArrayList<ResourceState>();
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
    public void testNullPort() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testNullPortDB");

        ResourceState server = new DefaultResourceState();
        server.putProperty("host", "127.0.0.1");
        server.putProperty("port", null);
        List<ResourceState> serversResourceState = new ArrayList<ResourceState>();
        serversResourceState.add(server);

        config.putProperty("servers", serversResourceState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH );

        assertThat(result.getProperty("db")).isEqualTo("testNullPortDB");
        assertThat(result.getProperty("servers")).isNotNull();

        List<ResourceState> servers = (List) result.getProperty("servers");
        assertThat(servers.size()).isEqualTo(1);
        assertThat(servers.get(0).getProperty("host")).isEqualTo("127.0.0.1"); //the default host
        assertThat(servers.get(0).getProperty("port")).isEqualTo(27017); //the default port
    }

    @Test
    public void testUpdateServers() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateServersDB");

        List<ResourceState> servers = new ArrayList<ResourceState>();

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

        ResourceState updatedServerB = new DefaultResourceState();
        updatedServerB.putProperty("host", "127.0.0.3");
        updatedServerB.putProperty("port", 12345); // max port number that is valid
        updatedServers.add(updatedServerB);

        updatedConfig.putProperty("servers", updatedServers);


        ResourceState result = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updatedConfig);

        List<ResourceState> serversResourceState = (List) result.getProperty("servers");
        assertThat(serversResourceState.size()).isEqualTo(1);
        assertThat(serversResourceState.get(0).getProperty("host")).isEqualTo("127.0.0.3");
        assertThat(serversResourceState.get(0).getProperty("port")).isEqualTo(12345);

    }

}
