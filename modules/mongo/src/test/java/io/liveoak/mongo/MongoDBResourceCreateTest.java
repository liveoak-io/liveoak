/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceCreateTest extends BaseMongoDBTest {

    @Test
    public void testSimpleCreate() throws Exception {
        String methodName = "testSimpleCreate";
        assertFalse( db.collectionExists( methodName ) );
        db.createCollection( methodName, new BasicDBObject() );

        CloseableHttpResponse response = testSimplePostMethod( baseURL + "/" + methodName, "{\"foo\":\"bar\"}" );

        assertEquals( 201, response.getStatusLine().getStatusCode() );

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree( response.getEntity().getContent() );

        assertThat( jsonNode.get( "id" ).asText() ).isNotNull();
        assertThat( jsonNode.get( "foo" ).asText() ).isEqualTo( "bar" );
        assertThat( jsonNode.get( "self" ) ).isNotNull();

        // verify what is stored in the mongo db
        assertThat( db.collectionExists( methodName ) ).isTrue();
        assertThat( db.getCollection( methodName ).getCount() ).isEqualTo( 1 );

        DBObject dbObject = db.getCollection( methodName ).findOne();
        assertThat( dbObject.get( "foo" ) ).isEqualTo( "bar" );
    }

    @Test
    public void testSimpleCreateWithId() throws Exception {
        String methodName = "testSimpleCreateWithID";
        assertFalse( db.collectionExists( methodName ) );
        db.createCollection( methodName, new BasicDBObject() );

        CloseableHttpResponse response = testSimplePostMethod( baseURL + "/" + methodName, "{\"id\":\"helloworld\", \"foo\":\"bar\"}" );
        assertEquals( 201, response.getStatusLine().getStatusCode() );

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree( response.getEntity().getContent() );
        assertThat( jsonNode.get( "id" ).asText() ).isEqualTo( "helloworld" );
        assertThat( jsonNode.get( "foo" ).asText() ).isEqualTo( "bar" );
        assertThat( jsonNode.get( "self" ) ).isNotNull();

        // verify what is stored in the mongo db
        assertTrue( db.collectionExists( methodName ) );
        assertEquals( 1, db.getCollection( methodName ).getCount() );
        DBObject dbObject = db.getCollection( methodName ).findOne( new BasicDBObject( "_id", "helloworld" ) );
        assertEquals( "bar", dbObject.get( "foo" ) );
    }


    @Test
    public void testComplexCreate() throws Exception {
        String methodName = "testComplexCreate";
        assertFalse( db.collectionExists( methodName ) );
        db.createCollection( methodName, new BasicDBObject() );

        CloseableHttpResponse response = testSimplePostMethod( baseURL + "/" + methodName,
                "{ \"id\" : \"helloworld\",\n" +
                        "  \"foo\" : \"bar\",\n" +
                        "  \"test\" : \"123\",\n" +
                        "  \"arr\" : [1, 1, 2, 3, 5, 8, 13, 21],\n" +
                        "  \"obj\" : { \"foo2\" : \"bar2\", \"test2\" : \"123\", \"subobject\":{\"abc\" : \"xyz\"}}\n" +
                        "}\n" );
        assertEquals( 201, response.getStatusLine().getStatusCode() );

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        IOUtils.copy( response.getEntity().getContent(), System.out );

        //System.out.println("MONGO: " + db.getCollection(methodName).findOne());

        CloseableHttpResponse readResponse = testSimpleGetMethod( baseURL + "/" + methodName + "/helloworld/obj" );
        //System.out.println("READ OBJ: " + readResponse.getStatusLine().toString());
        //IOUtils.copy(readResponse.getEntity().getContent(), System.out);

//        readResponse = testSimpleGetMethod(baseURL + "/" + methodName + "/helloworld/arr");
//        System.out.println("READ ARR: " + readResponse.getStatusLine().toString());
//        IOUtils.copy(readResponse.getEntity().getContent(), System.out);

//        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
//        assertEquals(5, jsonNode.size()); // id, _self, bar
//        assertEquals("helloworld", jsonNode.get("id").asText());
//        assertEquals("bar", jsonNode.get("foo").asText());
//        assertEquals("test", jsonNode.get("123").asText());
//        assertEquals("[0,1,2,3,5,8,13]", jsonNode.get("arr").asText());
//        assertNotNull(jsonNode.get("_self"));
//
//        // verify what is stored in the mongo db
//        assertTrue(db.collectionExists(methodName));
//        assertEquals(1, db.getCollection(methodName).getCount());
//        DBObject dbObject = db.getCollection(methodName).findOne();
//        assertEquals("bar", dbObject.get("foo"));
    }


}
