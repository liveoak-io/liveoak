package io.liveoak.mongo.config;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.CreateNotSupportedException;
import io.liveoak.spi.exceptions.DeleteNotSupportedException;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDatastoreTest extends BaseMongoConfigTest {

    @Override
    public void loadExtensions() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree(
                        "{" +
                        "  internal-database: { db: 'testDataStoresDB', datastore: 'foo'}," +
                        "  datastores: {" +
                        "    foo: {servers: [{ host: 'localhost', port: 27018}]}," +
                        "    bar: {servers: [{ port: 27017}]},"  +
                        "    baz: {}" +
                        "  }" +
                        "}");

        loadExtension("mongo", new MongoExtension(), (ObjectNode)configNode);
    }

    @Test
    public void testSystemConfigRead() throws Exception {
        ResourceState systemConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);

        ResourceState internalDB = systemConfigState.getProperty("internal-database", true, ResourceState.class);
        assertThat(internalDB.getProperty("db")).isEqualTo("testDataStoresDB");
        assertThat(internalDB.getProperty("datastore")).isEqualTo("foo");

        ResourceState datastores = systemConfigState.getProperty("datastores", true, ResourceState.class);

        ResourceState foo = datastores.getProperty("foo", true, ResourceState.class);
        ResourceState fooServer = (ResourceState) foo.getProperty("servers", true, List.class).get(0);
        assertThat(fooServer.getProperty("host")).isEqualTo("localhost");
        assertThat(fooServer.getProperty("port")).isEqualTo(27018);

        ResourceState bar = datastores.getProperty("bar", true, ResourceState.class);
        ResourceState barServer = (ResourceState) bar.getProperty("servers", true, List.class).get(0);
        assertThat(barServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(barServer.getProperty("port")).isEqualTo(27017);

        ResourceState baz = datastores.getProperty("baz", true, ResourceState.class);
        ResourceState bazServer = (ResourceState) baz.getProperty("servers", true, List.class).get(0);
        assertThat(bazServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(bazServer.getProperty("port")).isEqualTo(27017);
    }

    @Test
    public void testSystemConfigUpdate() throws Exception {
        ResourceState systemConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);

        // Try and update with what we got from the read, just to make sure an exception doesn't occur here.
        client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, systemConfigState);

        ResourceState dataStores = systemConfigState.getProperty("datastores", true, ResourceState.class);

        //modify bar
        ResourceState updatedBar = dataStores.getProperty("bar", true, ResourceState.class);
        ResourceState updatedBarServer = (ResourceState) updatedBar.getProperty("servers", true, List.class).get(0);
        updatedBarServer.putProperty("host", "localhost");

        //delete baz
        dataStores.removeProperty("baz");

        //create bort
        dataStores.putProperty("bort", new DefaultResourceState());

        ResourceState updateState =  client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, systemConfigState);
        ResourceState updatedConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);

        ResourceState internalDB = updatedConfigState.getProperty("internal-database", true, ResourceState.class);
        assertThat(internalDB.getProperty("db")).isEqualTo("testDataStoresDB");
        assertThat(internalDB.getProperty("datastore")).isEqualTo("foo");

        ResourceState datastores = updatedConfigState.getProperty("datastores", true, ResourceState.class);

        ResourceState foo = datastores.getProperty("foo", true, ResourceState.class);
        ResourceState fooServer = (ResourceState) foo.getProperty("servers", true, List.class).get(0);
        assertThat(fooServer.getProperty("host")).isEqualTo("localhost");
        assertThat(fooServer.getProperty("port")).isEqualTo(27018);

        ResourceState bar = datastores.getProperty("bar", true, ResourceState.class);
        ResourceState barServer = (ResourceState) bar.getProperty("servers", true, List.class).get(0);
        assertThat(barServer.getProperty("host")).isEqualTo("localhost");
        assertThat(barServer.getProperty("port")).isEqualTo(27017);

        assertThat(dataStores.getProperty("baz")).isNull();

        ResourceState bort = datastores.getProperty("bort", true, ResourceState.class);
        ResourceState bortServer = (ResourceState) bort.getProperty("servers", true, List.class).get(0);
        assertThat(bortServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(bortServer.getProperty("port")).isEqualTo(27017);

        ResourceState updatedIDB = systemConfigState.getProperty("internal-database", true, ResourceState.class);
        updatedIDB.putProperty("datastore", "bar");

        updateState = client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, systemConfigState);
        assertThat(updateState.getProperty("internal-database", true, ResourceState.class).getProperty("datastore")).isEqualTo("foo");
    }

    @Test (expected = DeleteNotSupportedException.class)
    public void testSystemConfigDelete() throws Exception {
        client.delete(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
    }

    @Test (expected = CreateNotSupportedException.class)
    public void testSystemConfigCreate() throws Exception {
        client.create(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, new DefaultResourceState());
    }

    @Test
    public void testDatastore() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'foo'}");

        setUpSystem((ObjectNode)configNode);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        assertThat(result.getProperty("db")).isEqualTo("testDataStore");
        assertThat(result.getProperty("datastore")).isEqualTo("foo");
        assertThat(result.getProperty("servers")).isNull();
    }

    @Test (expected = InitializationException.class)
    public void testNullDatastore() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: null}");
        setUpSystem((ObjectNode)configNode);
    }

    @Test (expected = InitializationException.class)
    public void testEmptyDatastore() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: ''}");
        setUpSystem((ObjectNode)configNode);
    }

    @Test (expected = InitializationException.class)
    public void testInvalidDatastore() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'bat'}");
        setUpSystem((ObjectNode)configNode);
    }

    @Test
    public void testUpdateDatastore() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'foo'}");

        setUpSystem((ObjectNode)configNode);
        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        result.putProperty("datastore", "bar");
        ResourceState update = client.update(new RequestContext.Builder().build(), ADMIN_PATH, result);

        assertThat(update.members()).isNotNull();
        assertThat(update.members().size()).isEqualTo(0);

        assertThat(update.getProperty("db")).isEqualTo("testDataStore");
        assertThat(update.getProperty("datastore")).isEqualTo("bar");
        assertThat(update.getProperty("servers")).isNull();
    }

    @Test
    public void testChangeType() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'foo'}");

        setUpSystem((ObjectNode)configNode);

        ResourceState updateState1 = new DefaultResourceState();
        updateState1.putProperty("db", "testDataStore");

        ResourceState update1 = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updateState1);

        assertThat(update1.members()).isNotNull();
        assertThat(update1.members().size()).isEqualTo(0);

        assertThat(update1.getProperty("db")).isEqualTo("testDataStore");
        assertThat(update1.getProperty("datastore")).isNull();

        ResourceState server = (ResourceState) update1.getProperty("servers", true, List.class).get(0);
        assertThat(server.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(server.getProperty("port")).isEqualTo(27017);

        ResourceState updateState2 = new DefaultResourceState();
        updateState2.putProperty("db", "testDataStore");
        updateState2.putProperty("datastore", "bar");

        ResourceState update2 = client.update(new RequestContext.Builder().build(), ADMIN_PATH, updateState2);

        assertThat(update2.members()).isNotNull();
        assertThat(update2.members().size()).isEqualTo(0);

        assertThat(update2.getProperty("db")).isEqualTo("testDataStore");
        assertThat(update2.getProperty("datastore")).isEqualTo("bar");
        assertThat(update2.getProperty("servers")).isNull();

    }
}
