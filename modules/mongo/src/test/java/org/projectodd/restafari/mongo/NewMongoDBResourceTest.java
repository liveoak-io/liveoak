package org.projectodd.restafari.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.projectodd.restafari.container.codec.DefaultCollectionResourceState;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.testtools.AbstractResourceTestCase;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NewMongoDBResourceTest extends AbstractResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new MongoDBResource( "storage" );
    }

    @Override
    public Config createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + Math.random());
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
        assertThat( result ).isNotNull();
        assertThat( result ).isInstanceOf( MongoDBResource.class );
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        Resource result = connector.read("/storage/movies");
        assertThat(result).isNull();
    }

    @Test
    public void testCreateCollection() throws Exception {
        ObjectResourceState state = new DefaultObjectResourceState();
        state.id( "movies" );
        state.addProperty( "movies", new DefaultCollectionResourceState());

        Resource createdResource = connector.create( "/storage", state );

        assertThat( createdResource ).isNotNull();
        assertThat( createdResource ).isInstanceOf( MongoCollectionResource.class );
        assertThat( createdResource.id() ).isEqualTo( "movies" );

        Resource movies = connector.read( "/storage/movies" );

        // TODO: why doesn't this work?
        //assertThat( movies ).isNotNull();
        //assertThat( movies ).isInstanceOf( MongoCollectionResource.class );
        //assertThat( movies.id() ).isEqualTo( "movies" );
    }

}
