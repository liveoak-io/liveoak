package org.projectodd.restafari.mongo;

import org.junit.Test;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.codec.DefaultCollectionResourceState;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.ResourceNotFoundException;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.testtools.AbstractResourceTestCase;

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
        return new MongoDBResource("storage");
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
        Resource result = connector.read("/storage");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(MongoDBResource.class);
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        try {
            connector.read("/storage/movies");
            fail( "shouldn't get here" );
        } catch (ResourceNotFoundException e) {
            assertThat( e.path() ).isEqualTo( "/storage/movies" );
        }
    }

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
        //Resource movies = connector.read("/storage/movies");

        //assertThat( movies ).isNotNull();
        //assertThat( movies ).isInstanceOf( MongoCollectionResource.class );
        //assertThat( movies.id() ).isEqualTo( "movies" );
    }

}
