package io.liveoak.mongo.config;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.exceptions.ResourceException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
* @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
* @author Ken Finnigan
*/
public class MongoDatastoreTest extends BaseMongoConfigTest {

    @Override
    public void loadExtensions() throws Exception {
        JsonNode configNode = ObjectMapperFactory.create().readTree(
                        "{ db: 'testDataStoresDB', datastore: 'foo'}");

        JsonNode instancesNode = ObjectMapperFactory.create().readTree(
                 "{" +
                 "    foo: {servers: [{ host: 'localhost', port: 27018}]}," +
                 "    bar: {servers: [{ port: 27017}]},"  +
                 "    baz: {}" +
                 "}");

        loadExtension("mongo", new MongoExtension(), (ObjectNode)configNode, (ObjectNode)instancesNode);
    }

    @Test
    public void testAll() throws Exception {
        testReadDataStoreInstances();
        testCreateDataStore();
        testDeleteDataStore();
        testUpdateDataStore();

        testDataStore();
        testNullDataStore();
        testEmptyDataStore();
        testInvalidDatastore();
        testChangeDataStore();
        testChangeType();
    }

    //@Test
    public void testReadDataStoreInstances() throws Exception {
        // TEST #1 - System config read
        ResourceState systemConfigState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);

        assertThat(systemConfigState.getProperty("db")).isEqualTo("testDataStoresDB");
        assertThat(systemConfigState.getProperty("datastore")).isEqualTo("foo");

        ResourceState instancesConfig = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH);

        assertThat(instancesConfig.members().size()).isEqualTo(4);

        ResourceState foo = null, bar = null, baz = null;

        for (ResourceState member : instancesConfig.members()) {
            if (member.id().equals("foo")) {
                foo = member;
            } else if (member.id().equals("bar")) {
                bar = member;
            } else if (member.id().equals("baz")) {
                baz = member;
            } else if (member.id().equals("module")) {
                // ignore the module member for these tests
            } else {
                fail();
            }
        }

        assertThat(foo.id()).isEqualTo("foo");
        ResourceState fooServer = (ResourceState) foo.getProperty("servers", true, List.class).get(0);
        assertThat(fooServer.getProperty("host")).isEqualTo("localhost");
        assertThat(fooServer.getProperty("port")).isEqualTo(27018);

        //ResourceState bar = instancesConfig.members().get(1);
        assertThat(bar.id()).isEqualTo("bar");
        ResourceState barServer = (ResourceState) bar.getProperty("servers", true, List.class).get(0);
        assertThat(barServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(barServer.getProperty("port")).isEqualTo(27017);

        //ResourceState baz = instancesConfig.members().get(2);
        assertThat(baz.id()).isEqualTo("baz");
        ResourceState bazServer = (ResourceState) baz.getProperty("servers", true, List.class).get(0);
        assertThat(bazServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(bazServer.getProperty("port")).isEqualTo(27017);
    }

    //@Test
    public void testCreateDataStore() throws Exception {

        ResourceState bortResourceState = new DefaultResourceState("bort");
        ResourceState createResponse = client.create(new RequestContext.Builder().build(),INSTANCES_CONFIG_PATH, bortResourceState );
        assertThat(createResponse.id()).isEqualTo("bort");

        ResourceState readState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH);
        assertThat(readState.members().size()).isEqualTo(5);

        ResourceState foo = null, bar = null, baz = null, bort = null;

        for (ResourceState member : readState.members()) {
            if (member.id().equals("foo")) {
                foo = member;
            } else if (member.id().equals("bar")) {
                bar = member;
            } else if (member.id().equals("baz")) {
                baz = member;
            } else if (member.id().equals("bort")) {
                bort = member;
            } else if (member.id().equals("module")) {
                // ignore the module member for these tests
            } else {
                fail();
            }
        }

        if (foo == null || bar == null || baz == null || bort == null) {
            fail();
        }

        assertThat(bort.id()).isEqualTo("bort");
        ResourceState bortServer = (ResourceState) bort.getProperty("servers", true, List.class).get(0);
        assertThat(bortServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(bortServer.getProperty("port")).isEqualTo(27017);

    }

    //@Test
    public void testDeleteDataStore() throws Exception {
        ResourceState deleteState = client.delete(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH + "/foo");
        assertThat(deleteState.id()).isEqualTo("foo");

        ResourceState readState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH);

        ResourceState foo = null, bar = null, baz = null, bort = null;

        for (ResourceState member : readState.members()) {
            if (member.id().equals("bar")) {
                bar = member;
            } else if (member.id().equals("baz")) {
                baz = member;
            } else if (member.id().equals("bort")) {
                bort = member;
            } else if (member.id().equals("module")) {
                // ignore the module member for these tests
            } else {
                fail();
            }
        }

        if (bar == null || baz == null || bort == null) {
            fail();
        }

        assertThat(foo).isNull();
    }

    //@Test
    public void testUpdateDataStore() throws Exception {
        ResourceState barResourceState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH + "/bar");

        List servers = barResourceState.getProperty("servers", false, List.class);
        ((ResourceState)servers.get(0)).putProperty("port", 27019);

        client.update(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH + "/bar", barResourceState);

        ResourceState readState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), INSTANCES_CONFIG_PATH + "/bar");

        assertThat(readState).isNotNull();
        assertThat(readState.id()).isEqualTo("bar");
        ResourceState barServer = (ResourceState) readState.getProperty("servers", true, List.class).get(0);
        assertThat(barServer.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(barServer.getProperty("port")).isEqualTo(27019);
    }

    //@Test
    public void testDataStore() throws Exception {
        // TEST #5 - Test datastore
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'foo'}");

        InternalApplicationExtension resource = setUpSystem((ObjectNode) configNode);

        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        assertThat(result.members()).isNotNull();
        assertThat(result.members().size()).isEqualTo(0);

        assertThat(result.getProperty("db")).isEqualTo("testDataStore");
        assertThat(result.getProperty("datastore")).isEqualTo("foo");
        assertThat(result.getProperty("servers")).isNull();

        // Reset for next test
        removeResource(resource);
    }



    //@Test
    public void testNullDataStore() throws Exception {
        // TEST #6 - Null datastore
        try {
            JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: null}");
            setUpSystem((ObjectNode) configNode);
            fail("InitializationException should have been thrown");
        } catch (InitializationException ie) {
            // Expected
            this.system.awaitStability();
        }
    }

    //@Test
    public void testEmptyDataStore() throws Exception {
        // TEST #7 - Empty datastore
        try {
            JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: ''}");
            setUpSystem((ObjectNode) configNode);
            fail("InitializationException should have been thrown");
        } catch (InitializationException ie) {
            // Expected
            this.system.awaitStability();
        }
    }

    //@Test
    public void testInvalidDatastore() throws Exception {
        // TEST #8 - Invalid datastore
        ResourceState updateState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        updateState.putProperty("datastore", "bat"); //bat doesn't exist as a datastore

        try {
            ResourceState updatedState = client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, updateState);
            fail();
        } catch (ResourceException e) {
            //expected
        }
    }

    //@Test
    public void testChangeDataStore() throws Exception {
        // TEST #9 - Update datastore
        JsonNode configNode = ObjectMapperFactory.create().readTree("{ db: 'testDataStore', datastore: 'foo'}");

        InternalApplicationExtension resource = setUpSystem((ObjectNode) configNode);
        ResourceState result = client.read(new RequestContext.Builder().build(), ADMIN_PATH);

        result.putProperty("datastore", "bar");
        ResourceState update = client.update(new RequestContext.Builder().build(), ADMIN_PATH, result);

        assertThat(update.members()).isNotNull();
        assertThat(update.members().size()).isEqualTo(0);

        assertThat(update.getProperty("db")).isEqualTo("testDataStore");
        assertThat(update.getProperty("datastore")).isEqualTo("bar");
        assertThat(update.getProperty("servers")).isNull();

        // Reset for next test
        removeResource(resource);
    }



    //@Test
    public void testChangeType() throws Exception {
        // TEST #10 - Change type
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
