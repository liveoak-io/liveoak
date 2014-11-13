package io.liveoak.container;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class PutIdHandlingTest extends AbstractContainerTest {
    private static Client client;

    @BeforeClass
    public static void setUp() throws Exception {
        system = LiveOakFactory.create();
        client = system.client();

        awaitStability();

        InternalApplication app = system.applicationRegistry().createApplication("testApp", "Test Application");
        system.extensionInstaller().load("db", new InMemoryDBExtension());
        app.extend("db");

        InMemoryDBResource db = (InMemoryDBResource) system.service(InMemoryDBExtension.resource("testApp", "db"));
        db.addMember(new CollectionResource(db, "people"));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        system.stop();
    }

    @Test
    public void test() throws Exception {
        ResourceState state = new DefaultResourceState();
        state.putProperty("name", "Barry Manilow");

        state = client.update(new RequestContext.Builder().build(), "/testApp/db/people/barry", state);

        assertThat(state).isNotNull();
        assertThat(state.id()).isEqualTo("barry");
        assertThat(state.getPropertyAsString("name")).isEqualTo("Barry Manilow");

        state.putProperty("name", "Barry Feingold");

        state = client.update(new RequestContext.Builder().build(), "/testApp/db/people/barry", state);

        assertThat(state).isNotNull();
        assertThat(state.id()).isEqualTo("barry");
        assertThat(state.getPropertyAsString("name")).isEqualTo("Barry Feingold");
    }

    public static class CollectionResource implements SynchronousResource {
        public CollectionResource(Resource parent, String id) {
            this.parent = parent;
            this.id = id;
        }

        @Override
        public Resource parent() {
            return parent;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Resource member(RequestContext ctx, String id) {
            return this.collection.get(id);
        }

        @Override
        public Collection<Resource> members(RequestContext ctx) {
            return this.collection.values().stream().collect(Collectors.toList());
        }

        @Override
        public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
            String id = state.id();
            Resource r = new ObjectResource(this, id, cleanse(state));
            this.collection.put(id, r);
            responder.resourceCreated(r);
        }

        protected ResourceState cleanse(ResourceState state) {
            state.removeProperty(LiveOak.ID);
            state.removeProperty(LiveOak.SELF);
            return state;
        }

        private Resource parent;
        private String id;
        private Map<String, Resource> collection = new HashMap<>();
    }

    public static class ObjectResource implements SynchronousResource {

        public ObjectResource(CollectionResource parent, String id, ResourceState state) {
            this.parent = parent;
            this.id = id;
            try {
                properties(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Resource parent() {
            return parent;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public void properties(ResourceState props) throws Exception {
            if (props != null) {
                for (String key : props.getPropertyNames()) {
                    this.props.put(key, props.getProperty(key));
                }
            }
        }

        @Override
        public Map<String, ?> properties(RequestContext ctx) throws Exception {
            return this.props;
        }

        private CollectionResource parent;
        private String id;
        private Map<String, Object> props = new HashMap<>();
    }
}
