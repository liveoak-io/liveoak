package org.projectodd.restafari.vertx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.spi.ResourceException;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.testtools.AbstractResourceTestCase;
import org.projectodd.restafari.vertx.adapter.CollectionResourceAdapter;
import org.projectodd.restafari.vertx.resource.RootVertxCollectionResource;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class VertxResourceTest extends AbstractResourceTestCase {

    private CollectionResourceAdapter adapter;
    private Map<String, JsonObject> objects = new HashMap<>();

    @Override
    public RootResource createRootResource() {
        return new RootVertxCollectionResource("vertx", "test.vertx");
    }

    @Before
    public void setUp() {
        // resources for our collection
        objects.put("bob", new JsonObject().putString("id", "bob").putString("name", "Bob McWhirter"));
        objects.put("ben", new JsonObject().putString("id", "ben").putString("name", "Ben Browning"));

        adapter = new CollectionResourceAdapter(vertx, "test.vertx");

        // read a single item from the collection
        adapter.readMemberHandler((id, responder) -> {
            JsonObject object = objects.get(id);

            if (object != null) {
                responder.resourceRead(object);
            } else {
                responder.noSuchResource(id);
            }
        });

        // read the collection
        adapter.readMembersHandler((responder) -> {
            JsonArray resources = new JsonArray(objects.values().toArray());
            responder.resourcesRead(resources);
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
        List<Resource> resources = new ArrayList<>();
        CompletableFuture<List<Resource>> future = new CompletableFuture<>();
        ResourceSink sink = new ResourceSink() {
            @Override
            public void close() {
                future.complete(resources);
            }

            @Override
            public void accept(Resource resource) {
                resources.add(resource);
            }
        };
        CollectionResource resource = (CollectionResource) connector.read("/vertx");
        assertThat(resource).isNotNull();
        resource.readContent(null, sink);
        List<Resource> result = future.get();
        assertThat(result.size()).isEqualTo(2);
    }

}
