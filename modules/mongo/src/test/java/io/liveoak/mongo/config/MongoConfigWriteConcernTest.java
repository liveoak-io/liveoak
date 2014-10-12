package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class MongoConfigWriteConcernTest extends BaseMongoConfigTest {

    @Test
    public void testDefault() throws Exception {
        // TEST #1 - Default
        ResourceState config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");
        InternalApplicationExtension resource = setUpSystem(config);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();

        ResourceState writeConcernState = (ResourceState) result.getProperty(WriteConcernState.ID);
        assertThat(writeConcernState.getProperty(WriteConcernState.W)).isEqualTo(1);
        assertThat(writeConcernState.getProperty(WriteConcernState.WTIMEOUT)).isEqualTo(0);
        assertThat(writeConcernState.getProperty(WriteConcernState.J)).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernState.FSYNC)).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernState.CONTINUEONERRORFORINSERT)).isEqualTo(false);

        // Reset for next test
        removeResource(resource);


        // TEST #2 - Setting values
        config = new DefaultResourceState();
        config.putProperty("db", "testDefaultDB");

        ResourceState configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernState.W, 2);
        configWriteConcernState.putProperty(WriteConcernState.WTIMEOUT, 100);
        configWriteConcernState.putProperty(WriteConcernState.J, true);
        configWriteConcernState.putProperty(WriteConcernState.FSYNC, true);
        configWriteConcernState.putProperty(WriteConcernState.CONTINUEONERRORFORINSERT, true);

        config.putProperty(WriteConcernState.ID, configWriteConcernState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testDefaultDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();

        writeConcernState = (ResourceState) result.getProperty(WriteConcernState.ID);
        assertThat(writeConcernState.getProperty(WriteConcernState.W)).isEqualTo(2);
        assertThat(writeConcernState.getProperty(WriteConcernState.WTIMEOUT)).isEqualTo(100);
        assertThat(writeConcernState.getProperty(WriteConcernState.J)).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernState.FSYNC)).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernState.CONTINUEONERRORFORINSERT)).isEqualTo(true);

        // Reset for next test
        removeResource(resource);


        // TEST #3 - Tagged Write Concern
        config = new DefaultResourceState();
        config.putProperty("db", "testTaggedDB");

        configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernState.W, "majority");
        configWriteConcernState.putProperty(WriteConcernState.WTIMEOUT, 250);
        configWriteConcernState.putProperty(WriteConcernState.J, false);
        configWriteConcernState.putProperty(WriteConcernState.FSYNC, true);
        configWriteConcernState.putProperty(WriteConcernState.CONTINUEONERRORFORINSERT, false);

        config.putProperty(WriteConcernState.ID, configWriteConcernState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testTaggedDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();

        writeConcernState = (ResourceState) result.getProperty(WriteConcernState.ID);
        assertThat(writeConcernState.getProperty(WriteConcernState.W)).isEqualTo("majority");
        assertThat(writeConcernState.getProperty(WriteConcernState.WTIMEOUT)).isEqualTo(250);
        assertThat(writeConcernState.getProperty(WriteConcernState.J)).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernState.FSYNC)).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernState.CONTINUEONERRORFORINSERT)).isEqualTo(false);

        // Reset for next test
        removeResource(resource);


        // TEST #4 - Update Write Concern
        config = new DefaultResourceState();
        config.putProperty("db", "testUpdateWriteConcernDB");

        configWriteConcernState = new DefaultResourceState();
        configWriteConcernState.putProperty(WriteConcernState.W, 2);
        configWriteConcernState.putProperty(WriteConcernState.WTIMEOUT, 100);
        configWriteConcernState.putProperty(WriteConcernState.J, true);
        configWriteConcernState.putProperty(WriteConcernState.FSYNC, true);
        configWriteConcernState.putProperty(WriteConcernState.CONTINUEONERRORFORINSERT, true);

        config.putProperty(WriteConcernState.ID, configWriteConcernState);

        resource = setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testUpdateWriteConcernDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();

        writeConcernState = (ResourceState) result.getProperty(WriteConcernState.ID);
        assertThat(writeConcernState.getProperty(WriteConcernState.W)).isEqualTo(2);
        assertThat(writeConcernState.getProperty(WriteConcernState.WTIMEOUT)).isEqualTo(100);
        assertThat(writeConcernState.getProperty(WriteConcernState.J)).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernState.FSYNC)).isEqualTo(true);
        assertThat(writeConcernState.getProperty(WriteConcernState.CONTINUEONERRORFORINSERT)).isEqualTo(true);

        // Reset for next test
        removeResource(resource);


        // TEST #5 - Null Write Concern
        config = new DefaultResourceState();
        config.putProperty("db", "testTaggedDB");

        config.putProperty(WriteConcernState.ID, null);

        setUpSystem(config);

        result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.getProperty("db")).isEqualTo("testTaggedDB");
        assertThat(result.getProperty("servers")).isNotNull();

        assertThat(result.getProperty(WriteConcernState.ID)).isNotNull();

        writeConcernState = (ResourceState) result.getProperty(WriteConcernState.ID);
        assertThat(writeConcernState.getProperty(WriteConcernState.W)).isEqualTo(1);
        assertThat(writeConcernState.getProperty(WriteConcernState.WTIMEOUT)).isEqualTo(0);
        assertThat(writeConcernState.getProperty(WriteConcernState.J)).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernState.FSYNC)).isEqualTo(false);
        assertThat(writeConcernState.getProperty(WriteConcernState.CONTINUEONERRORFORINSERT)).isEqualTo(false);
    }
}

