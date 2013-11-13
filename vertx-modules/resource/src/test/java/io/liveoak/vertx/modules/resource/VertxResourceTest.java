package io.liveoak.vertx.modules.resource;

import org.junit.Before;
import org.junit.Test;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.DirectConnector;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.vertx.modules.server.ResourceDeployer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.fest.assertions.Assertions.assertThat;
import static junit.framework.Assert.fail;


/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class VertxResourceTest  {

    private DefaultContainer container;
    private ResourceDeployer deployer;
    private DirectConnector connector;
    private Map<String, JsonObject> objects = new HashMap<>();
    private CollectionResourceAdapter adapter;


    @Before
    public void setUpContainer() throws Exception {
        this.container = new DefaultContainer();
        this.connector = this.container.directConnector();
        this.deployer = new ResourceDeployer( this.container, "test.register" );
    }

    @Before
    public void setUpResource() throws Exception {
        this.adapter = new CollectionResourceAdapter(container.vertx(), "people", "test.register");

        this.objects.put("bob", new JsonObject().putString("id", "bob").putString("name", "Bob McWhirter"));
        this.objects.put("ben", new JsonObject().putString("id", "ben").putString("name", "Ben Browning"));
        this.adapter.readMemberHandler((id, responder) -> {

            System.err.println( "asked for: " + id );
            JsonObject object = objects.get(id);

            if (object != null) {
                responder.resourceRead(object);
            } else {
                responder.noSuchResource(id);
            }
        });

        this.adapter.readMembersHandler((responder) -> {
            JsonArray resources = new JsonArray(objects.values().toArray());
            responder.resourcesRead(resources);
        });

        this.adapter.start();
        Thread.sleep( 500 );
    }

    @Test
    public void testReadMember() throws Exception {
        Resource result = connector.read("/people/bob");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("bob");
        assertThat(result.uri().toString()).isEqualTo("/people/bob");
        assertThat(result.parent().id()).isEqualTo("people");
    }


    /*
    @Test
    public void testReadMemberProperty() throws Exception {
        Resource result = connector.read("/people/bob/name");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PropertyResource.class);
        assertThat(((PropertyResource) result).get(null)).isEqualTo("Bob McWhirter");
    }
    */

    @Test
    public void testReadNonExistentResource() {
        try {
            connector.read("/people/lance");
        } catch (ResourceException e) {
            assertThat(e.path()).isEqualTo("/people/lance");
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

        Resource resource = connector.read("/people");
        assertThat(resource).isNotNull();
        resource.readMembers(null, sink);
        List<Resource> result = future.get();
        assertThat(result.size()).isEqualTo(2);

    }

}
