package io.liveoak.mongo;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.AbstractResourceTestCase;

import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NewMongoDBResourceTest extends AbstractResourceTestCase {

    //NOTE: will be soon be removed. Do not add any test classes here anymore //

    @Override
    public RootResource createRootResource() {
        return new RootMongoResource("storage");
    }

    @Override
    public Config createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        return config;
    }

    @Test
    public void testRootFound() throws Exception {
        ResourceState result = connector.read( new RequestContext.Builder().build(), "/storage");
        assertThat(result).isNotNull();
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        try {
            connector.read( new RequestContext.Builder().build(), "/storage/movies");
            fail( "shouldn't get here" );
        } catch (ResourceNotFoundException e) {
            assertThat( e.path() ).isEqualTo( "/storage/movies" );
        }
    }

    /*
    @Test
    public void testCreateCollection() throws Exception {
        ObjectResourceState state = new DefaultObjectResourceState();
        state.id("movies");
        state.addProperty("movies", new DefaultCollectionResourceState());

        Resource createdResource = connector.create("/storage", state);

        assertThat(createdResource).isNotNull();
        assertThat(createdResource).isInstanceOf(MongoCollectionResource.class);
        assertThat(createdResource.id()).isEqualTo("movies");

        // TODO: why doesn't this work?
        //Resource movies = connector.readMember("/storage/movies");

        //assertThat( movies ).isNotNull();
        //assertThat( movies ).isInstanceOf( MongoCollectionResource.class );
        //assertThat( movies.id() ).isEqualTo( "movies" );
    }
    */

}
