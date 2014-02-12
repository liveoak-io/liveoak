package io.liveoak.mongo.config;

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
    public void testDefault() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsResource.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsResource.ID);
        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.DESCRIPTION.toString())).isEqualTo( "liveoak" );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECTIONS_PER_HOST.toString())).isEqualTo( 100 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER.toString())).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_WAIT_TIME.toString())).isEqualTo( 120000 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECT_TIMEOUT.toString())).isEqualTo( 10000 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString())).isEqualTo( false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.AUTOCONNECT_RETRY.toString())).isEqualTo( false );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_AUTOCONNECT_RETRY_TIME.toString())).isEqualTo( new Long(0) );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CURSOR_FINALIZER_ENABLED.toString())).isEqualTo( true );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.ALWAYS_USE_MBEANS.toString())).isEqualTo( false );
    }

    @Test
    public void testConfigure() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testConfigureDB");

        ResourceState mongoClientConfigResourceState = new DefaultResourceState();
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.DESCRIPTION.toString(), "my cool mbaas" );
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.MAX_AUTOCONNECT_RETRY_TIME.toString(), new Long(50) );
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString(), true );

        config.putProperty( MongoClientOptionsResource.ID, mongoClientConfigResourceState);


        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testConfigureDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsResource.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsResource.ID);
        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECTIONS_PER_HOST.toString())).isEqualTo(100);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER.toString())).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_WAIT_TIME.toString())).isEqualTo(120000 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECT_TIMEOUT.toString())).isEqualTo( 10000 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.AUTOCONNECT_RETRY.toString())).isEqualTo( false );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CURSOR_FINALIZER_ENABLED.toString())).isEqualTo( true );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.ALWAYS_USE_MBEANS.toString())).isEqualTo( false );

        // the modified values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.DESCRIPTION.toString())).isEqualTo( "my cool mbaas" );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_AUTOCONNECT_RETRY_TIME.toString())).isEqualTo( new Long(50) );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString())).isEqualTo( true);

    }

    @Test
    public void testUpdateConfigure() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateConfigureDB");

        ResourceState mongoClientConfigResourceState = new DefaultResourceState();
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.DESCRIPTION.toString(), "my cool mbaas" );
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.MAX_AUTOCONNECT_RETRY_TIME.toString(), new Long(50) );
        mongoClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString(), true );

        config.putProperty( MongoClientOptionsResource.ID, mongoClientConfigResourceState);

        setUpSystem( config );

        ResourceState updatedClientConfigResourceState = new DefaultResourceState( );
        updatedClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.DESCRIPTION.toString(), "the awesome mbaas" );
        updatedClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString(), false );
        updatedClientConfigResourceState.putProperty( MongoClientOptionsResource.Options.MAX_WAIT_TIME.toString(), 1000 );

        ResourceState updatedConfig = new DefaultResourceState();
        updatedConfig.putProperty( MongoClientOptionsResource.ID,updatedClientConfigResourceState);

        ResourceState result = client.update( new RequestContext.Builder().build(), BASEPATH + ";config", updatedConfig );

        assertThat( result.getProperty( "db" ) ).isEqualTo("testUpdateConfigureDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(MongoClientOptionsResource.ID)).isNotNull();
        ResourceState mongoClientOptionResource = (ResourceState) result.getProperty(MongoClientOptionsResource.ID);

        // the default values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECTIONS_PER_HOST.toString())).isEqualTo(100);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.THREADS_ALLOWED_TO_BLOCK_FOR_CONNECTION_MULTIPLIER.toString())).isEqualTo(5);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CONNECT_TIMEOUT.toString())).isEqualTo( 10000 );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.AUTOCONNECT_RETRY.toString())).isEqualTo( false );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.CURSOR_FINALIZER_ENABLED.toString())).isEqualTo( true );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.ALWAYS_USE_MBEANS.toString())).isEqualTo( false );
        
        // the updated values
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.DESCRIPTION.toString())).isEqualTo( "the awesome mbaas" );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_AUTOCONNECT_RETRY_TIME.toString())).isEqualTo( new Long(50) );
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.SOCKET_KEEP_ALIVE.toString())).isEqualTo( false);
        assertThat(mongoClientOptionResource.getProperty(MongoClientOptionsResource.Options.MAX_WAIT_TIME.toString())).isEqualTo( 1000);
    }
}
