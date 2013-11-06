package org.projectodd.restafari.vertx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.spi.ResourceException;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.testtools.AbstractResourceTestCase;
import org.projectodd.restafari.vertx.adapter.CollectionResourceAdapter;
import org.projectodd.restafari.vertx.resource.RootVertxCollectionResource;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class VertxResourceTest extends AbstractResourceTestCase {

    CollectionResourceAdapter adapter;
    Map<String, JsonObject> objects = new HashMap<>();

    @Override
    public RootResource createRootResource() {
        return new RootVertxCollectionResource("vertx", "test.vertx");
    }

    @Before
    public void setUp() {
        adapter = new CollectionResourceAdapter(vertx, "test.vertx");
        objects.put("bob", new JsonObject().putString("id", "bob").putString("name", "Bob McWhirter"));
        objects.put("ben", new JsonObject().putString("id", "ben").putString("name", "Ben Browning"));
        adapter.readMemberHandler((id, responder) -> {
            JsonObject object = objects.get(id);

            if (object != null) {
                responder.resourceRead(object);
            } else {
                responder.noSuchResource(id);
            }
        });
        adapter.start();
    }

    @After
    public void tearDown() {
        adapter.stop();
    }

    @Test
    public void testReadMember() throws Exception {
        Resource result = connector.read("/vertx/bob");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("bob");
        assertThat(result.uri().toString()).isEqualTo("/vertx/bob");
        assertThat(result.parent().id()).isEqualTo("vertx");
    }

    @Test
    public void testReadMemberProperty() throws Exception {
        Resource result = connector.read("/vertx/bob/name");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PropertyResource.class);
        assertThat(((PropertyResource) result).get(null)).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testReadNonExistentResource() {
        try {
            connector.read("/vertx/lance");
        } catch (ResourceException e) {
            assertThat(e.path()).isEqualTo("/vertx/lance");
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testReadMembers() throws Exception {

        adapter.readMembersHandler((responder) -> {
            JsonArray resources = new JsonArray(objects.values().toArray());
            responder.resourcesRead(resources);
        });

        CollectionResource resource = (CollectionResource) connector.read("/vertx");
    }

}
