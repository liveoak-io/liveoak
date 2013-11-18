/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import io.liveoak.container.SimpleConfig;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.Config;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

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
        return new RootMongoResource( "storage" );
    }

    @Override
    public Config createConfig() {
        String database = System.getProperty( "mongo.db", "MongoControllerTest_" + UUID.randomUUID() );
        Integer port = new Integer( System.getProperty( "mongo.port", "27017" ) );
        String host = System.getProperty( "mongo.host", "localhost" );

        SimpleConfig config = new SimpleConfig();
        config.put( "db", database );
        config.put( "port", port );
        config.put( "host", host );

        return config;
    }

    @Test
    public void testRootFound() throws Exception {
        ResourceState result = connector.read( new RequestContext.Builder().build(), "/storage" );
        assertThat( result ).isNotNull();
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        try {
            connector.read( new RequestContext.Builder().build(), "/storage/movies" );
            fail( "shouldn't get here" );
        } catch ( ResourceNotFoundException e ) {
            assertThat( e.path() ).isEqualTo( "/storage/movies" );
        }
    }

    @Test
    public void testCreateCollection() throws Exception {
        // check that we can create the resource
        ResourceState state = new DefaultResourceState( "movies" );
        ResourceState createdResource = connector.create( new RequestContext.Builder().build(), "/storage", state );
        assertThat( createdResource ).isNotNull();
        assertThat( createdResource.id() ).isEqualTo( "movies" );

        // test that we get this resource back on a read
        ResourceState movies = connector.read( new RequestContext.Builder().build(), "/storage/movies" );
        assertThat( movies ).isNotNull();
        assertThat( movies.id() ).isEqualTo( "movies" );
    }

}
