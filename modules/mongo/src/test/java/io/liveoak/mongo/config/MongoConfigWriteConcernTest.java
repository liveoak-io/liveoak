package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigWriteConcernTest extends BaseMongoConfigTest {

    @Test
    public void testDefault() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        setUpSystem( config );

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState)result.getProperty(WriteConcernResource.ID);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo(1);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(0);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(false);
    }

    @Test
    public void testSettingValues() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");

        ResourceState configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernResource.Options.W.toString(), 2);
        configWriteConcernState.putProperty(WriteConcernResource.Options.WTIMEOUT.toString(), 100);
        configWriteConcernState.putProperty(WriteConcernResource.Options.J.toString(), true);
        configWriteConcernState.putProperty(WriteConcernResource.Options.FSYNC.toString(), true);
        configWriteConcernState.putProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString(), true);

        config.putProperty(WriteConcernResource.ID, configWriteConcernState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");


        assertThat( result.getProperty( "db" ) ).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState)result.getProperty(WriteConcernResource.ID);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo(2);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(100);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(true);
    }

    @Test
    public void testTaggedWriteConcern() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testTaggedDB");

        ResourceState configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernResource.Options.W.toString(), "majority");
        configWriteConcernState.putProperty(WriteConcernResource.Options.WTIMEOUT.toString(), 250);
        configWriteConcernState.putProperty(WriteConcernResource.Options.J.toString(), false);
        configWriteConcernState.putProperty(WriteConcernResource.Options.FSYNC.toString(), true);
        configWriteConcernState.putProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString(), false);

        config.putProperty(WriteConcernResource.ID, configWriteConcernState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testTaggedDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState)result.getProperty(WriteConcernResource.ID);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo("majority");
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(250);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(false);
    }

    @Test
    public void testUpdateWriteConcern() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testUpdateWriteConcernDB");

        ResourceState configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernResource.Options.W.toString(), 2);
        configWriteConcernState.putProperty(WriteConcernResource.Options.WTIMEOUT.toString(), 100);
        configWriteConcernState.putProperty(WriteConcernResource.Options.J.toString(), true);
        configWriteConcernState.putProperty(WriteConcernResource.Options.FSYNC.toString(), true);
        configWriteConcernState.putProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString(), true);

        config.putProperty(WriteConcernResource.ID, configWriteConcernState);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testUpdateWriteConcernDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState)result.getProperty(WriteConcernResource.ID);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo(2);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(100);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(true);

        ResourceState updateWriteConcernState = new DefaultResourceState();
        ResourceState updatedConfigWriteConcernState = new DefaultResourceState();
        updatedConfigWriteConcernState.putProperty(WriteConcernResource.Options.W.toString(), "majority");
        updatedConfigWriteConcernState.putProperty(WriteConcernResource.Options.J.toString(), false );
        updateWriteConcernState.putProperty(WriteConcernResource.ID, updatedConfigWriteConcernState);

        ResourceState updatedResult = client.update(new RequestContext.Builder().build(), BASEPATH + ";config", updateWriteConcernState);

        ResourceState updatedWCResult = (ResourceState) updatedResult.getProperty(WriteConcernResource.ID);
        assertThat(updatedWCResult.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo("majority");
        assertThat(updatedWCResult.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(100);
        assertThat(updatedWCResult.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(false);
        assertThat(updatedWCResult.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(true);
        assertThat(updatedWCResult.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(true);
    }

    @Test
    public void testNullWriteConcern() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testTaggedDB");

        config.putProperty(WriteConcernResource.ID, null);

        setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");

        assertThat( result.getProperty( "db" ) ).isEqualTo("testTaggedDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernResource.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState)result.getProperty(WriteConcernResource.ID);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.W.toString())).isEqualTo(1);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.WTIMEOUT.toString())).isEqualTo(0);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.J.toString())).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.FSYNC.toString())).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernResource.Options.CONTINUEONERRORFORINSERT.toString())).isEqualTo(false);
    }
}

