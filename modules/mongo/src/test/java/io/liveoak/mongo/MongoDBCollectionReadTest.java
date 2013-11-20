/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionReadTest extends NewBaseMongoDBTest {

    @Test
    public void testGetStorageEmpty() throws Exception {
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertThat( db.getCollectionNames() ).hasSize( 0 );

        ResourceState result = connector.read( new RequestContext.Builder().build(), BASEPATH );

        // verify response
        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( BASEPATH );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members() ).isEmpty();
    }


    @Test
    public void testGetStorageCollections() throws Exception {
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertThat( db.getCollectionNames() ).hasSize( 0 );

        // create a couple of collections
        db.createCollection( "collection1", new BasicDBObject() );
        db.createCollection( "collection2", new BasicDBObject() );
        db.createCollection( "collection3", new BasicDBObject() );

        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertThat( db.getCollectionNames() ).hasSize( 4 );


        ResourceState result = connector.read( new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) ).build(), BASEPATH );

        // verify response
        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( BASEPATH );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members().size() ).isEqualTo( 3 );

        for ( int i = 0; i < result.members().size(); i++ ) {
            ResourceState member = result.members().get( i );
            assertThat( member.id() ).isEqualTo( "collection" + ( i + 1 ) );
            assertThat( member.getPropertyNames().size() ).isEqualTo( 1 );
            assertThat( member.getProperty( "type" ) ).isEqualTo( "collection" );
            assertThat( member.members() ).isEmpty();
        }
    }


    @Test
    public void testGetEmptyCollection() throws Exception {
        String methodName = "testEmptyCollection";
        // check that the collection really is empty
        assertFalse( db.collectionExists( methodName ) );

        try {
            ResourceState result = connector.read( new RequestContext.Builder().build(), BASEPATH + "/" + methodName );
            Fail.fail();
        } catch ( ResourceNotFoundException e ) {
            //expected
        }

        db.createCollection( methodName, new BasicDBObject() );

        ResourceState result = connector.read( new RequestContext.Builder().build(), BASEPATH + "/" + methodName );

        //verify the result
        assertThat( result.id() ).isEqualTo( methodName );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members() ).isEmpty();

        // check that the collection is still empty
        assertEquals( 0, db.getCollection( methodName ).getCount() );
    }

    @Test
    public void testGetStorageCollectionsPagination() throws Exception {
        // DB db = mongoClient.getDB("testGetStorageCollectionsPagination");
        db.dropDatabase();
        assertEquals( 0, db.getCollectionNames().size() );
        // create a bunch of collections
        for ( int i = 0; i < 1013; i++ ) {
            db.createCollection( String.format( "collection%1$04d", i ), new BasicDBObject( "count", i ) );
        }
        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertEquals( 1014, db.getCollectionNames().size() );

        // This should return 23 collections
        RequestContext requestContext = new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) ).pagination( new SimplePagination( 11, 23 ) ).build();
        ResourceState result = connector.read( requestContext, BASEPATH );

        //verify the result
        assertThat( result.id() ).isEqualTo( BASEPATH );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members().size() ).isEqualTo( 23 );

        for ( int i = 0; i < result.members().size(); i++ ) {
            ResourceState member = result.members().get( i );
            assertThat( member.id() ).isEqualTo( String.format( "collection%1$04d", i + 11 ) );
            assertThat( member.getPropertyNames().size() ).isEqualTo( 1 );
            assertThat( member.getProperty( "type" ) ).isEqualTo( "collection" );
            assertThat( member.members() ).isEmpty();
        }

        // This should return 3 collections as a total number of them is 1013
        requestContext = new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) ).pagination( new SimplePagination( 1010, 20 ) ).build();
        result = connector.read( requestContext, BASEPATH );

        //verify the result
        assertThat( result.id() ).isEqualTo( BASEPATH );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members().size() ).isEqualTo( 3 );

        for ( int i = 0; i < result.members().size(); i++ ) {
            ResourceState member = result.members().get( i );
            assertThat( member.id() ).isEqualTo( String.format( "collection%1$04d", i + 1010 ) );
            assertThat( member.getPropertyNames().size() ).isEqualTo( 1 );
            assertThat( member.getProperty( "type" ) ).isEqualTo( "collection" );
            assertThat( member.members() ).isEmpty();
        }
    }

    @Test
    public void testGetStorageCollectionsQuery() throws Exception {

        DBCollection collection = db.getCollection( "testQueryCollection" );
        if ( collection != null ) {
            collection.drop();
        }
        collection = db.createCollection( "testQueryCollection", new BasicDBObject( "count", 0 ) );

        // insert data records for the test
        setupPeopleData( collection );
        assertThat( collection.count() ).isEqualTo( 6 );

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put( "q", "{lastName:{$gt:'E', $lt:'R'}}" );
        RequestContext requestContext = new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) ).resourceParams( resourceParams ).build();
        ResourceState result = connector.read( requestContext, BASEPATH + "/testQueryCollection" );

        // verify response
        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( "testQueryCollection" );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members().size() ).isEqualTo( 2 );

        assertThat( result.members().get( 0 ).getProperty( "name" ) ).isEqualTo( "Hans" );
        assertThat( result.members().get( 1 ).getProperty( "name" ) ).isEqualTo( "Francois" );

        //Try another query
        resourceParams = new SimpleResourceParams();
        resourceParams.put( "q", "{lastName:'Doe'}" );
        requestContext = new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) ).resourceParams( resourceParams ).build();
        result = connector.read( requestContext, BASEPATH + "/testQueryCollection" );

        assertThat( result ).isNotNull();
        assertThat( result.id() ).isEqualTo( "testQueryCollection" );
        assertThat( result.getPropertyNames().size() ).isEqualTo( 1 );
        assertThat( result.getProperty( "type" ) ).isEqualTo( "collection" );
        assertThat( result.members().size() ).isEqualTo( 2 );

        assertThat( result.members().get( 0 ).getProperty( "name" ) ).isEqualTo( "John" );
        assertThat( result.members().get( 1 ).getProperty( "name" ) ).isEqualTo( "Jane" );
    }

    @Test
    public void testGetStorageCollectionsSort() throws Exception {

        DBCollection collection = db.getCollection( "testSortCollection" );
        if ( collection != null ) {
            collection.drop();
        }
        collection = db.createCollection( "testSortCollection", new BasicDBObject( "count", 0 ) );

        // insert data records for the test
        setupPeopleData( collection );
        assertThat( collection.count() ).isEqualTo( 6 );

        // going through DirectConnector will bypass phase where container sets up Pagination, Sorting, ReturnFields
        // so resourceParams are only relevant for 'q' parameter
        SimpleResourceParams resourceParams = new SimpleResourceParams();

        // This should return 6 items ordered by lastName ascending, and name descending
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) )
                .sorting( new Sorting( "lastName,-name" ) )
                .resourceParams( resourceParams ).build();
        ResourceState result = connector.read( requestContext, BASEPATH + "/testSortCollection" );

        String[] expected = { "Jacqueline", "John", "Jane", "Hans", "Francois", "Helga" };
        assertThat( expected ).isEqualTo( getNames( result ) );
    }

    @Test
    public void testGetStorageCollectionsQueryAndSort() throws Exception {

        DBCollection collection = db.getCollection( "testQuerySortCollection" );
        if ( collection != null ) {
            collection.drop();
        }
        collection = db.createCollection( "testQuerySortCollection", new BasicDBObject( "count", 0 ) );

        // insert data records for the test
        setupPeopleData( collection );
        assertThat( collection.count() ).isEqualTo( 6 );

        // going through DirectConnector will bypass phase where container sets up Pagination, Sorting, ReturnFields
        // so resourceParams are only relevant for 'q' parameter
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put( "q", "{country:{$ne:'FR'}}" );

        // This should return 4 items ordered by lastName descending and name ascending
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields( new ReturnFieldsImpl( "*" ).withExpand( "members" ) )
                .sorting( new Sorting( "-lastName,name" ) )
                .resourceParams( resourceParams ).build();
        ResourceState result = connector.read( requestContext, BASEPATH + "/testQuerySortCollection" );

        String[] expected = { "Helga", "Hans", "Jane", "John" };
        assertThat( expected ).isEqualTo( getNames( result ) );
    }

    private String[] getNames( ResourceState result ) {
        List<String> ret = new LinkedList<>();
        for ( ResourceState item : result.members() ) {
            ret.add( ( String ) item.getProperty( "name" ) );
        }
        return ret.toArray( new String[ret.size()] );
    }

    private void setupPeopleData( DBCollection collection ) {
        // add a few people
        String[][] data = {
                { "John", "Doe", "US", "San Francisco" },
                { "Jane", "Doe", "US", "New York" },
                { "Hans", "Gruber", "DE", "Berlin" },
                { "Helga", "Schmidt", "DE", "Munich" },
                { "Francois", "Popo", "FR", "Marseille" },
                { "Jacqueline", "Coco", "FR", "Paris" }
        };

        addPeopleItems( collection, data );
    }

    private void addPeopleItems( DBCollection collection, String[][] data ) {
        for ( String[] rec : data ) {
            BasicDBObject obj = new BasicDBObject();
            obj.put( "name", rec[0] );
            obj.put( "lastName", rec[1] );
            obj.put( "country", rec[2] );
            obj.put( "city", rec[3] );

            collection.insert( obj );

            System.err.println( "INSERT: " + obj );
        }
    }

    private class SimplePagination implements Pagination {

        int offset;
        int limit;

        public SimplePagination( int offset, int limit ) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int limit() {
            return limit;
        }
    }

    private class SimpleResourceParams implements ResourceParams {

        Map<String, String> map = new HashMap<String, String>();

        public void put( String name, String value ) {
            map.put( name, value );
        }

        @Override
        public Collection<String> names() {
            return map.keySet();
        }

        @Override
        public boolean contains( String name ) {
            return map.containsKey( name );
        }

        @Override
        public String value( String name ) {
            return map.get( name );
        }

        @Override
        public List<String> values( String name ) {
            List list = new ArrayList<String>();
            list.add( map.get( name ) );
            return list;
        }

        @Override
        public int intValue( String name, int defaultValue ) {
            return defaultValue;
        }
    }
}
