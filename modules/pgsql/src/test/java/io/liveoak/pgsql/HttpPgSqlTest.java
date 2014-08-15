package io.liveoak.pgsql;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * See superclass JavaDoc for how to set up PostgreSQL for this test.
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpPgSqlTest extends BasePgSqlHttpTest {

    @Test
    public void testAll() throws IOException {
        System.out.println("testAll");
        testInitialCollections();

        // create an address
        testCreateFirstAddress();

        // create an order
        testCreateFirstOrder();

        // create another order
        testCreateSecondOrder();

        testSortLimitAndOffset();

        // create items table
        testCreateItemsCollection();

        // create a new item
        testCreateFirstOrderItem();

        // read all orders expanded
        testReadOrdersExpanded();
        testReadOrdersDoubleExpanded();

        // query orders
        testQueryOrders();

        // delete an order cascading - include all the order items
        testDeleteFirstOrderCascading();

        // delete items collection
        testDeleteOrderItemsCollection();

        // create first order again
        testCreateFirstOrder();

        // GET all orders and send them back to _batch?action=delete, then send them to _batch?action=create
        testBulkOrdersDeleteAndCreateBySendingGetResponse();

        // recreate items table
        testCreateItemsCollection();

        // create attachments table
        testCreateAttachmentsCollection();

        // Bulk update by sending deeply nested to _batch?action=update
        testBulkUpdateNested();

        // GET all collections and send response back to _batch?action=delete,
        // then use _batch?action=create to recreate them
        testBulkTablesDeleteBySendingGetResponse();
    }

    private void testQueryOrders() throws IOException {
        // we use Mongo query syntax
        String query = "{total: {$gt: 30000}}";

        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        query = "{$or: [{create_date: {$gt: '2014-04-03'}}, {$not: {total: {$gt: 30000}}}]}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        //
        query = "{$and: [{create_date: {$lt: '2014-04-03'}}, {total: {$gt: 30000}}]}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        // this query is equivalent to the previous one
        query = "{create_date: {$lt: '2014-04-03'}, total: {$gt: 30000}}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        // condition requiring joins
        query = "{'address.country_iso': 'UK'}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        // condition requiring joins
        query = "{'items.name': 'The Gadget'}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        // condition requiring multiple joins
        query = "{'items.name': 'The Gadget', 'address.country_iso': 'UK'}";

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?q=" + URLEncoder.encode(query, "utf-8"));
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);
        checkResultForError(result);

        // TODO: proper response checking to make sure queries return proper results
    }

    private void testBulkTablesDeleteBySendingGetResponse() throws IOException {

        // send the response as a POST to /_batch endpoint
        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH + "/_batch?action=delete");
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);


        // first try to delete one table with dependencies so it should fail
        String json = "{                                                             \n" +
                "  'members' : [ {                                                   \n" +
                "    'id' : 'addresses',                                             \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/addresses'                         \n" +
                "    }                                                               \n" +
                "  } ]                                                               \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        // Check that there are still all the tables
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH);
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : 'sqldata',                                                 \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata'                                     \n" +
                "  },                                                                \n" +
                "  'links': [{                                                       \n" +
                "    'rel': 'batch',                                                 \n" +
                "    'href': '/testApp/sqldata/_batch'                               \n" +
                "  }],                                                               \n" +
                "  'count' : 5,                                                      \n" +
                "  'type' : 'database',                                              \n" +
                "  'members' : [ {                                                   \n" +
                "    'id' : 'addresses',                                             \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/addresses'                         \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : 'attachments',                                           \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/attachments'                       \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : 'items',                                                 \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/items'                             \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : '" + schema + ".orders',                                 \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/" + schema + ".orders'             \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : '" + schema_two + ".orders',                             \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders'         \n" +
                "    }                                                               \n" +
                "  } ]                                                               \n" +
                "}";

        checkResult(result, expected);

        // fetch all schemas for these tables
        List<JsonNode> schemas = fetchSchemas("addresses", "attachments", "items", schema + ".orders", schema_two + ".orders");

        // delete them all now
        result = postRequest(post, expected);
        System.out.println(result);

        // check that they are deleted
        result = getRequest(get);
        System.out.println(result);

        expected = "{                                                                \n" +
                "  'id' : 'sqldata',                                                 \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata'                                     \n" +
                "  },                                                                \n" +
                "  'links': [{                                                       \n" +
                "    'rel': 'batch',                                                 \n" +
                "    'href': '/testApp/sqldata/_batch'                               \n" +
                "  }],                                                               \n" +
                "  'count' : 0,                                                      \n" +
                "  'type' : 'database'                                               \n" +
                "}";

        checkResult(result, expected);

        // now use _batch endpoint to recreate them
        StringBuilder sb = new StringBuilder("{ \"members\": [");
        int i = 0;
        for (JsonNode node: schemas) {
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(node.toString());
            i++;
        }
        sb.append("]}");

        json = sb.toString();
        System.out.println("request: " + json);

        post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH + "/_batch?action=create");
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);

        result = postRequest(post, json);
        System.out.println(result);

        checkResultForError(result);

        // TODO: test error response when trying to recreate table with FKs pointing to nonexistent table
        // TODO: test batch recreating multiple tables on top of existing multiple tables
        //   e.g. addresses, orders exists, create attachments, items
    }

    private List<JsonNode> fetchSchemas(String ... tableIds) throws IOException {
        List<JsonNode> results = new LinkedList<>();
        for (String tableId: tableIds) {
            HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + tableId + ";schema");
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            String result = getRequest(get);
            System.out.println(result);

            results.add(parseJson(result));
        }
        return results;
    }

    private void testBulkOrdersDeleteAndCreateBySendingGetResponse() throws IOException {

        // get all orders
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?sort=id&fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        String orders = result;
        String expected = "{                                                                     \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 2,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                               \n" +
                "    'id' : '014-1003095',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-1003095',                                                 \n" +
                "    'create_date' : 1402146615000,                                              \n" +
                "    'total' : 18990,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    }                                                                           \n" +
                "  }, {                                                                          \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    }                                                                           \n" +
                "  } ]                                                                           \n" +
                "}";

        checkResult(result, expected);

        // send the response as a POST to /_batch endpoint
        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH + "/_batch?action=delete");
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);

        result = postRequest(post, result);
        System.out.println(result);

        String expectedBatch = "{                                                                \n" +
                "  'id' : '_batch',                                                              \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/_batch'                                          \n" +
                "  }                                                                             \n" +
                "}";

        checkResult(result, expectedBatch);


        // fetch all orders again - there should be none
        result = getRequest(get);
        System.out.println(result);

        expected = "{                                                                            \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 0,                                                                  \n" +
                "  'type' : 'collection'                                                         \n" +
                "}";

        checkResult(result, expected);


        // now recreate them
        post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH + "/_batch?action=create");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        get.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        result = orders;
        result = postRequest(post, result);
        System.out.println(result);

        checkResult(result, expectedBatch);   // we should get results back - all created members

        // fetch all orders again - there should be two, like when we started
        result = getRequest(get);
        System.out.println(result);

        checkResult(result, orders);
    }

    private void testBulkUpdateNested() throws IOException {

        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH + "/_batch?action=update");
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);

        // update orders by passing a collection object (container doesn't support top level arrays)
        String updatedOrders = "{                                                                \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 2,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                               \n" +
                "    'id' : '014-1003095',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-1003095',                                                 \n" +
                "    'create_date' : 1402146615000,                                              \n" +
                "    'total' : 18990,                                                            \n" +
                "    'items' : [ {                                                               \n" +
                "      'id': 'I39845355',                                                        \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/items/I39845355'                             \n" +
                "      },                                                                        \n" +
                "      'item_id': 'I39845355',                                                   \n" +
                "      'name': 'The Gadget',                                                     \n" +
                "      'quantity': 1,                                                            \n" +
                "      'price': 39900,                                                           \n" +
                "      'vat': 20,                                                                \n" +
                "      'attachments': [ {                                                        \n" +
                "        'id': 'att000001',                                                      \n" +
                "        'self' : {                                                              \n" +
                "          'href' : '/testApp/sqldata/attachments/att000001'                     \n" +
                "        },                                                                      \n" +
                "        'attachment_id': 'att000001',                                           \n" +
                "        'name' : 'specs.doc',                                                   \n" +
                "        'content' : 'Lorem Ipsum ...'                                           \n";

        // we don't send this line, but we receive it
        // this is to test that master back link insertion works as it should
        String updatedAttachmentBackLink = ", 'item': { 'self': { 'href': '/testApp/sqldata/items/I39845355'}} \n";

        String updatedOrdersB = "      } ],                                                       \n" +
                "      'order' : {                                                               \n" +
                "        'self' : {                                                              \n" +
                "          'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'     \n" +
                "        }                                                                       \n" +
                "      }                                                                         \n" +
                "    } ],                                                                        \n" +
                "    'address' : {                                                               \n" +
                "      'id': '1',                                                                \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      },                                                                        \n" +
                "      'address_id': 1,                                                          \n" +
                "      'name': 'John F. Doe',                                                    \n" +
                "      'street': 'Liveoak street 7',                                             \n" +
                "      'postcode': null,                                                         \n" +
                "      'city': 'London',                                                         \n" +
                "      'country_iso': 'UK',                                                      \n" +
                "      'is_company': false,                                                      \n" +
                "      '" + schema + ".orders': [ ],                                             \n" +
                "      '" + schema_two + ".orders' : [ {                                         \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-1003095'      \n" +
                "        }                                                                       \n" +
                "      },{                                                                       \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-2004096'      \n" +
                "        }                                                                       \n" +
                "      } ]                                                                       \n" +
                "    }                                                                           \n" +
                "  }, {                                                                          \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'items' : [ ],                                                              \n" +
                "    'address' : {                                                               \n" +
                "      'id': '1',                                                                \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      },                                                                        \n" +
                "      'address_id': 1,                                                          \n" +
                "      'name': 'John F. Doe',                                                    \n" +
                "      'street': 'Liveoak street 7',                                             \n" +
                "      'postcode': null,                                                         \n" +
                "      'city': 'London',                                                         \n" +
                "      'country_iso': 'UK',                                                      \n" +
                "      'is_company': false,                                                      \n" +
                "      '" + schema + ".orders': [ ],                                             \n" +
                "      '" + schema_two + ".orders' : [ {                                         \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-1003095'      \n" +
                "        }                                                                       \n" +
                "      },{                                                                       \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-2004096'      \n" +
                "        }                                                                       \n" +
                "      } ]                                                                       \n" +
                "    }                                                                           \n" +
                "  } ]                                                                           \n" +
                "}";

        String result = postRequest(post, updatedOrders + updatedOrdersB);
        System.out.println(result);

        String expectedBatch = "{                                                                \n" +
                "  'id' : '_batch',                                                              \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/_batch'                                          \n" +
                "  }                                                                             \n" +
                "}";

        checkResult(result, expectedBatch);   // we should get results back - all updated members


        // get all orders, must be the same as the value of 'updatedOrders' - including 'addresses', and 'items'
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?sort=id&fields=*(*(*),items(*,attachments(*,-items),order),addresses(*," + schema + ".orders," + schema_two + ".orders))");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);

        String expected = updatedOrders + updatedAttachmentBackLink + updatedOrdersB;
        checkResult(result, expected);
    }

    private void testCreateAttachmentsCollection() throws IOException {
        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'attachments',                                              \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'attachment_id',                                     \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'content',                                           \n" +
                "       'type': 'text',                                              \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'item_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['attachment_id'],                                 \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': 'items',                                             \n" +
                "      'columns': ['item_id']                                        \n" +
                "   }]                                                               \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': 'attachments;schema',                                       \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata/attachments;schema'                  \n" +
                "   },                                                               \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'attachment_id',                                     \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'content',                                           \n" +
                "       'type': 'text',                                              \n" +
                "       'size': 2147483647,                                          \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'item_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['attachment_id'],                                 \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': '" + schema + ".items',                              \n" +
                "      'columns': ['item_id']                                        \n" +
                "   }],                                                              \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"attachments\" (\"attachment_id\" varchar (40), \"name\" varchar (255) NOT NULL, " +
                        "\"content\" text NOT NULL, \"item_id\" varchar (40) NOT NULL, PRIMARY KEY (\"attachment_id\"), FOREIGN KEY (\"item_id\") " +
                        "REFERENCES \"" + schema + "\".\"items\" (\"item_id\"))'     \n" +
                "}";

        checkResult(result, expected);
    }

    private void testSortLimitAndOffset() throws IOException {
        // get orders with limit 1, offset 1, sorted by total
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?sort=total&offset=1&limit=1&fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                                     \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 1,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                              \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    }                                                                           \n" +
                "  } ]                                                                           \n" +
                "}";

        checkResult(result, expected);

        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?sort=-total&offset=1&limit=1&fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);

        expected = "{                                                                            \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 1,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                              \n" +
                "    'id' : '014-1003095',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-1003095',                                                 \n" +
                "    'create_date' : 1402146615000,                                              \n" +
                "    'total' : 18990,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    }                                                                           \n" +
                "  } ]                                                                           \n" +
                "}";

        checkResult(result, expected);
    }

    private void testDeleteOrderItemsCollection() throws IOException {
        HttpDelete delete = new HttpDelete("http://localhost:8080/testApp/" + BASEPATH + "/items");
        delete.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = deleteRequest(delete);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': 'items',                                                    \n" +
                "  'self': {                                                         \n" +
                "    'href': '/testApp/sqldata/items'                                \n" +
                "  }                                                                 \n" +
                "}";

        checkResult(result, expected);

        // check current collections ... there should be no more /items
        testInitialCollections();
    }

    private void testDeleteFirstOrderCascading() throws IOException {
        HttpDelete delete = new HttpDelete("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-1003095?cascade");
        delete.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = deleteRequest(delete);
        System.out.println(result);
        String expected = "{                                                                     \n" +
                "  'id': '014-1003095',                                                          \n" +
                "  'self': {                                                                     \n" +
                "    'href': '/testApp/sqldata/" + schema_two + ".orders/014-1003095'            \n" +
                "  }                                                                             \n" +
                "}";

        checkResult(result, expected);

        // query orders - should get back the second order only
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);

        expected = "{                                                                            \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 1,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                              \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    },                                                                          \n" +
                "    'items' : [ ]                                                               \n" +
                "  } ]                                                                           \n" +
                "}";
        checkResult(result, expected);

        // query order items - should get back no items
        get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/items?fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        result = getRequest(get);
        System.out.println(result);

        expected = "{                                                                            \n" +
                "  'id' : 'items',                                                               \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/items'                                           \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/items;schema'                                    \n" +
                "  }],                                                                           \n" +
                "  'count' : 0,                                                                  \n" +
                "  'type' : 'collection'                                                         \n" +
                "}";
        checkResult(result, expected);
    }

    private void testReadOrdersExpanded() throws IOException {
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?sort=total&fields=*(*)");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                                     \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 2,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                               \n" +
                "    'id' : '014-1003095',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-1003095',                                                 \n" +
                "    'create_date' : 1402146615000,                                              \n" +
                "    'total' : 18990,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    },                                                                          \n" +
                "    'items' : [ {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/items/I39845355'                             \n" +
                "      }                                                                         \n" +
                "    } ]                                                                         \n" +
                "  }, {                                                                          \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      }                                                                         \n" +
                "    },                                                                          \n" +
                "    'items' : [ ]                                                               \n" +
                "  } ]                                                                           \n" +
                "}";

        checkResult(result, expected);
    }

    private void testReadOrdersDoubleExpanded() throws IOException {
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH + "/" + schema_two + ".orders?fields=*(*(*))");
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                                     \n" +
                "  'id' : '" + schema_two + ".orders',                                           \n" +
                "  'self' : {                                                                    \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders'                       \n" +
                "  },                                                                            \n" +
                "  'links' : [{                                                                  \n" +
                "    'rel' : 'schema',                                                           \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'                \n" +
                "  }],                                                                           \n" +
                "  'count' : 2,                                                                  \n" +
                "  'type' : 'collection',                                                        \n" +
                "  'members' : [ {                                                               \n" +
                "    'id' : '014-1003095',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-1003095',                                                 \n" +
                "    'create_date' : 1402146615000,                                              \n" +
                "    'total' : 18990,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'id': '1',                                                                \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      },                                                                        \n" +
                "      'address_id': 1,                                                          \n" +
                "      'name': 'John F. Doe',                                                    \n" +
                "      'street': 'Liveoak street 7',                                             \n" +
                "      'postcode': null,                                                         \n" +
                "      'city': 'London',                                                         \n" +
                "      'country_iso': 'UK',                                                      \n" +
                "      'is_company': false,                                                      \n" +
                "      '" + schema + ".orders': [ ],                                             \n" +
                "      '" + schema_two + ".orders' : [ {                                         \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-1003095'      \n" +
                "        }                                                                       \n" +
                "      },{                                                                       \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-2004096'      \n" +
                "        }                                                                       \n" +
                "      } ]                                                                       \n" +
                "    },                                                                          \n" +
                "    'items' : [ {                                                               \n" +
                "      'id': 'I39845355',                                                        \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/items/I39845355'                             \n" +
                "      },                                                                        \n" +
                "      'item_id': 'I39845355',                                                   \n" +
                "      'name': 'The Gadget',                                                     \n" +
                "      'quantity': 1,                                                            \n" +
                "      'price': 39900,                                                           \n" +
                "      'vat': 20,                                                                \n" +
                "      'order' : {                                                               \n" +
                "        'self' : {                                                              \n" +
                "          'href' : '/testApp/sqldata/" + schema_two + ".orders/014-1003095'     \n" +
                "        }                                                                       \n" +
                "      }                                                                         \n" +
                "    } ]                                                                         \n" +
                "  }, {                                                                          \n" +
                "    'id' : '014-2004096',                                                       \n" +
                "    'self' : {                                                                  \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders/014-2004096'         \n" +
                "    },                                                                          \n" +
                "    'order_id' : '014-2004096',                                                 \n" +
                "    'create_date' : 1396429572000,                                              \n" +
                "    'total' : 43800,                                                            \n" +
                "    'address' : {                                                               \n" +
                "      'id': '1',                                                                \n" +
                "      'self' : {                                                                \n" +
                "        'href' : '/testApp/sqldata/addresses/1'                                 \n" +
                "      },                                                                        \n" +
                "      'address_id': 1,                                                          \n" +
                "      'name': 'John F. Doe',                                                    \n" +
                "      'street': 'Liveoak street 7',                                             \n" +
                "      'postcode': null,                                                         \n" +
                "      'city': 'London',                                                         \n" +
                "      'country_iso': 'UK',                                                      \n" +
                "      'is_company': false,                                                      \n" +
                "      '" + schema + ".orders': [ ],                                             \n" +
                "      '" + schema_two + ".orders' : [ {                                         \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-1003095'      \n" +
                "        }                                                                       \n" +
                "      },{                                                                       \n" +
                "        'self': {                                                               \n" +
                "          'href': '/testApp/sqldata/" + schema_two + ".orders/014-2004096'      \n" +
                "        }                                                                       \n" +
                "      } ]                                                                       \n" +
                "    },                                                                          \n" +
                "    'items' : [ ]                                                               \n" +
                "  } ]                                                                           \n" +
                "}";

        checkResult(result, expected);
    }

    private void testInitialCollections() throws IOException {
        HttpGet get = new HttpGet("http://localhost:8080/testApp/" + BASEPATH);
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : 'sqldata',                                                 \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata'                                     \n" +
                "  },                                                                \n" +
                "  'links': [{                                                       \n" +
                "    'rel': 'batch',                                                 \n" +
                "    'href': '/testApp/sqldata/_batch'                               \n" +
                "  }],                                                               \n" +
                "  'count' : 3,                                                      \n" +
                "  'type' : 'database',                                              \n" +
                "  'members' : [ {                                                   \n" +
                "    'id' : 'addresses',                                             \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/addresses'                         \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : '" + schema + ".orders',                                 \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/" + schema + ".orders'             \n" +
                "    }                                                               \n" +
                "  }, {                                                              \n" +
                "    'id' : '" + schema_two + ".orders',                             \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/testApp/sqldata/" + schema_two + ".orders'         \n" +
                "    }                                                               \n" +
                "  } ]                                                               \n" +
                "}";

        checkResult(result, expected);
    }

    private void testCreateItemsCollection() throws IOException {

        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'items',                                                    \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'item_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'quantity',                                          \n" +
                "       'type': 'int4',                                              \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'price',                                             \n" +
                "       'type': 'integer',                                           \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'vat',                                               \n" +
                "       'type': 'integer',                                           \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'order_id',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['item_id'],                                       \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': '" + schema_two + ".orders',                         \n" +
                "      'columns': ['order_id']                                       \n" +
                "   }]                                                               \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : 'items;schema',                                            \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata/items;schema'                        \n" +
                "  },                                                                \n" +
                "  'columns' : [ {                                                   \n" +
                "    'name' : 'item_id',                                             \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 40,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : true                                                 \n" +
                "  }, {                                                              \n" +
                "    'name' : 'name',                                                \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 255,                                                   \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'quantity',                                            \n" +
                "    'type' : 'int4',                                                \n" +
                "    'size' : 10,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'price',                                               \n" +
                "    'type' : 'int4',                                                \n" +
                "    'size' : 10,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'vat',                                                 \n" +
                "    'type' : 'int4',                                                \n" +
                "    'size' : 10,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'order_id',                                            \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 40,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  } ],                                                              \n" +
                "  'primary-key' : [ 'item_id' ],                                    \n" +
                "  'foreign-keys' : [ {                                              \n" +
                "    'table' : '" + schema_two + ".orders',                          \n" +
                "    'columns' : [ 'order_id' ]                                      \n" +
                "  } ],                                                              \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"items\" (\"item_id\" varchar (40), \"name\" varchar (255) NOT NULL, " +
                        "\"quantity\" int4 NOT NULL, \"price\" int4 NOT NULL, \"vat\" int4 NOT NULL, \"order_id\" varchar (40) NOT NULL, " +
                        "PRIMARY KEY (\"item_id\"), FOREIGN KEY (\"order_id\") REFERENCES \"" + schema_two + "\".\"orders\" (\"order_id\"))' \n" +
                "}";
        checkResult(result, expected);
    }

    private void testCreateFirstOrderItem() throws IOException {

        String endpoint = "/testApp/" + BASEPATH + "/items";

        String json = "{                                                                         \n" +
                "  'id': 'I39845355',                                                            \n" +
                "  'name': 'The Gadget',                                                         \n" +
                "  'quantity': 1,                                                                \n" +
                "  'price': 39900,                                                               \n" +
                "  'vat': 20,                                                                    \n" +
                "  'order': {                                                                    \n" +
                "    'id': '014-2004096',                                                        \n" +   // TODO: 'id' is silently ignored - maybe not ok
                "    'self': {                                                                   \n" +
                "      'href': '/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-1003095' \n" +
                "    }                                                                           \n" +
                "  }                                                                             \n" +
                "}";

        HttpPost post = new HttpPost("http://localhost:8080" + endpoint);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                                     \n" +
                "  'id': 'I39845355',                                                            \n" +
                "  'self': {                                                                     \n" +
                "    'href': '/testApp/" + BASEPATH + "/items/I39845355'                         \n" +
                "  },                                                                            \n" +
                "  'item_id': 'I39845355',                                                       \n" +
                "  'name': 'The Gadget',                                                         \n" +
                "  'quantity': 1,                                                                \n" +
                "  'price': 39900,                                                               \n" +
                "  'vat': 20,                                                                    \n" +
                "  'order': {                                                                    \n" +
                "    'self': {                                                                   \n" +
                "      'href': '/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-1003095' \n" +
                "    }                                                                           \n" +
                "  }                                                                             \n" +
                "}";

        checkResult(result, expected);
    }


    private void testCreateFirstAddress() throws IOException {
        String endpoint = "/testApp/" + BASEPATH + "/addresses";

        String json = "{                                                             \n" +
                "  'id': 1,                                                          \n" +
                "  'name': 'John F. Doe',                                            \n" +
                "  'street': 'Liveoak street 7',                                     \n" +
                "  'city': 'London',                                                 \n" +
                "  'country_iso': 'UK',                                              \n" +
                "  'is_company': false                                               \n" +
                "}";

        HttpPost post = new HttpPost("http://localhost:8080" + endpoint);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': '1',                                                        \n" +
                "  'self': {                                                         \n" +
                "    'href': '/testApp/" + BASEPATH + "/addresses/1'                 \n" +
                "  },                                                                \n" +
                "  'address_id': 1,                                                  \n" +
                "  'name': 'John F. Doe',                                            \n" +
                "  'street': 'Liveoak street 7',                                     \n" +
                "  'postcode': null,                                                 \n" +
                "  'city': 'London',                                                 \n" +
                "  'country_iso': 'UK',                                              \n" +
                "  'is_company': false,                                              \n" +
                "  '" + schema + ".orders': [],                                      \n" +
                "  '" + schema_two + ".orders': []                                   \n" +
                "}";

        checkResult(result, expected);
    }


    private void testCreateFirstOrder() throws IOException {
        String endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";

        String json = "{                                                             \n" +
                "  'id': '014-1003095',                                              \n" +
                "  'create_date': '2014-06-07T15:10:15',                             \n" +
                "  'total': 18990,                                                   \n" +
                "  'address': {                                                      \n" +
                "    'self': {                                                       \n" +
                "      'href': '/testApp/" + BASEPATH + "/addresses/1'               \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        HttpPost post = new HttpPost("http://localhost:8080" + endpoint);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = postRequest(post, json);
        System.out.println(result);

        String expected =  "{                                                        \n" +
                "  'id': '014-1003095',                                              \n" +
                "  'self': {                                                         \n" +
                "    'href': '/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-1003095'   \n" +
                "  },                                                                \n" +
                "  'order_id': '014-1003095',                                        \n" +
                "  'create_date': 1402146615000,                                     \n" +
                "  'total': 18990,                                                   \n" +
                "  'address': {                                                      \n" +
                "    'self': {                                                       \n" +
                "      'href': '/testApp/" + BASEPATH + "/addresses/1'               \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        checkResult(result, expected);
    }

    private void testCreateSecondOrder() throws IOException {
        String endpoint = "/testApp/" + BASEPATH + "/" + schema_two + ".orders";

        String json = "{                                                             \n" +
                "  'id': '014-2004096',                                              \n" +
                "  'create_date': '2014-04-02T11:06:12',                             \n" +
                "  'total': 43800,                                                   \n" +
                "  'address': {                                                      \n" +
                "    'self': {                                                       \n" +
                "      'href': '/testApp/" + BASEPATH + "/addresses/1'               \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        HttpPost post = new HttpPost("http://localhost:8080" + endpoint);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = postRequest(post, json);
        System.out.println(result);

        String expected =  "{                                                        \n" +
                "  'id': '014-2004096',                                              \n" +
                "  'self': {                                                         \n" +
                "    'href': '/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-2004096'   \n" +
                "  },                                                                \n" +
                "  'order_id': '014-2004096',                                        \n" +
                "  'create_date': 1396429572000,                                     \n" +
                "  'total': 43800,                                                   \n" +
                "  'address': {                                                      \n" +
                "    'self': {                                                       \n" +
                "      'href': '/testApp/" + BASEPATH + "/addresses/1'               \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        checkResult(result, expected);
    }

    @Before
    public void init() throws IOException {

        // create three tables
        HttpPost post = new HttpPost("http://localhost:8080/testApp/" + BASEPATH);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'addresses',                                                \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'address_id',                                        \n" +
                "       'type': 'integer'                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'street',                                            \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'postcode',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 10                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'city',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 60,                                                  \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'country_iso',                                       \n" +
                "       'type': 'char',                                              \n" +
                "       'size': 2                                                    \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'is_company',                                        \n" +
                "       'type': 'boolean',                                           \n" +
                "       'nullable': false,                                           \n" +
                "       'default': false                                             \n" +     // TODO: handle 'default'
                "     }],                                                            \n" +
                "  'primary-key': ['address_id']                                     \n" +
                "}";

        String result = postRequest(post, json);

        String expected = "{                                                         \n" +
                "  'id' : 'addresses;schema',                                        \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata/addresses;schema'                    \n" +
                "  },                                                                \n" +     // TODO: Add 'name': '$schema.address'
                "  'columns' : [ {                                                   \n" +
                "    'name' : 'address_id',                                          \n" +
                "    'type' : 'int4',                                                \n" +
                "    'size' : 10,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : true                                                 \n" +
                "  }, {                                                              \n" +
                "    'name' : 'name',                                                \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 255,                                                   \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'street',                                              \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 255,                                                   \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'postcode',                                            \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 10,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'city',                                                \n" +
                "    'type' : 'varchar',                                             \n" +
                "    'size' : 60,                                                    \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'country_iso',                                         \n" +
                "    'type' : 'bpchar',                                              \n" +
                "    'size' : 2,                                                     \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  }, {                                                              \n" +
                "    'name' : 'is_company',                                          \n" +
                "    'type' : 'bool',                                                \n" +
                "    'size' : 1,                                                     \n" +
                "    'nullable' : false,                                             \n" +
                "    'unique' : false                                                \n" +
                "  } ],                                                              \n" +     // TODO: handle 'default'
                "  'primary-key' : [ 'address_id' ],                                 \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"addresses\" (\"address_id\" int4, \"name\" varchar (255) NOT NULL, " +
                "\"street\" varchar (255) NOT NULL, \"postcode\" varchar (10) NOT NULL, \"city\" varchar (60) NOT NULL, " +
                "\"country_iso\" bpchar NOT NULL, \"is_company\" bool NOT NULL, PRIMARY KEY (\"address_id\"))' \n" +
                "}";

        checkResult(result, expected);


        // create orders
        json = "{                                                                    \n" +
                "  'id': 'orders',                                                   \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'order_id',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_date',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'total',                                             \n" +
                "       'type': 'int8',                                              \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'address_id',                                        \n" +
                "       'type': 'int4',                                              \n" +     // TODO - should work for integer as well
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['order_id'],                                      \n" +
                "  'foreign-keys': {                                                 \n" +
                "     'table': 'addresses',                                          \n" +
                "     'columns': ['address_id']                                      \n" +
                "  }                                                                 \n" +
                "}";

        result = postRequest(post, json);

        expected = "{                                                                \n" +
                "  'id' : 'orders;schema',                                           \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata/orders;schema'                       \n" +
                "  },                                                                \n" +
                "  'columns' : [                                                     \n" +
                "    {                                                               \n" +
                "      'name' : 'order_id',                                          \n" +
                "      'type' : 'varchar',                                           \n" +
                "      'size' : 40,                                                  \n" +
                "      'nullable' : false,                                           \n" +
                "      'unique' : true                                               \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'create_date',                                        \n" +
                "      'type': 'timestamp',                                          \n" +
                "      'size' : 29,                                                  \n" +
                "      'nullable' : false,                                           \n" +
                "      'unique' : false                                              \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'total',                                              \n" +
                "      'type': 'int8',                                               \n" +
                "      'size' : 19,                                                  \n" +
                "      'nullable': false,                                            \n" +
                "      'unique' : false                                              \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'address_id',                                         \n" +
                "      'type': 'int4',                                               \n" +     // TODO - should work for integer as well
                "      'size' : 10,                                                  \n" +
                "      'nullable': false,                                            \n" +
                "      'unique' : false                                              \n" +
                "    }],                                                             \n" +
                "  'primary-key': ['order_id'],                                      \n" +
                "  'foreign-keys': [{                                                \n" +
                "    'table': '" + schema + ".addresses',                            \n" +
                "    'columns': ['address_id']                                       \n" +
                "  }],                                                                \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"orders\" (" +
                        "\"order_id\" varchar (40), \"create_date\" timestamp NOT NULL, \"total\" int8 NOT NULL, \"address_id\" int4 NOT NULL, " +
                        "PRIMARY KEY (\"order_id\"), FOREIGN KEY (\"address_id\") REFERENCES \"" + schema + "\".\"addresses\" (\"address_id\"))' \n" +
                "}";

        checkResult(result, expected);

        // create another orders in a different schema
        json = "{                                                                    \n" +
                "  'id': '" + schema_two + ".orders',                                \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'order_id',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_date',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'total',                                             \n" +
                "       'type': 'int8',                                              \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'address_id',                                        \n" +
                "       'type': 'int4',                                              \n" +     // TODO - should work for integer as well
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['order_id'],                                      \n" +
                "  'foreign-keys': {                                                 \n" +
                "     'table': 'addresses',                                          \n" +
                "     'columns': ['address_id']                                      \n" +
                "  }                                                                 \n" +
                "}";

        result = postRequest(post, json);

        expected = "{                                                                \n" +
                "  'id' : '" + schema_two + ".orders;schema',                        \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/testApp/sqldata/" + schema_two + ".orders;schema'    \n" +
                "  },                                                                \n" +
                "  'columns' : [                                                     \n" +
                "    {                                                               \n" +
                "      'name' : 'order_id',                                          \n" +
                "      'type' : 'varchar',                                           \n" +
                "      'size' : 40,                                                  \n" +
                "      'nullable' : false,                                           \n" +
                "      'unique' : true                                               \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'create_date',                                        \n" +
                "      'type': 'timestamp',                                          \n" +
                "      'size' : 29,                                                  \n" +
                "      'nullable' : false,                                           \n" +
                "      'unique' : false                                              \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'total',                                              \n" +
                "      'type': 'int8',                                               \n" +
                "      'size' : 19,                                                  \n" +
                "      'nullable': false,                                            \n" +
                "      'unique' : false                                              \n" +
                "    },                                                              \n" +
                "    {                                                               \n" +
                "      'name': 'address_id',                                         \n" +
                "      'type': 'int4',                                               \n" +     // TODO - should work for integer as well
                "      'size' : 10,                                                  \n" +
                "      'nullable': false,                                            \n" +
                "      'unique' : false                                              \n" +
                "    }],                                                             \n" +
                "  'primary-key': ['order_id'],                                      \n" +
                "  'foreign-keys': [{                                                \n" +
                "    'table': '" + schema + ".addresses',                            \n" +
                "    'columns': ['address_id']                                       \n" +
                "  }],                                                               \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema_two + "\".\"orders\" (" +
                        "\"order_id\" varchar (40), \"create_date\" timestamp NOT NULL, \"total\" int8 NOT NULL, \"address_id\" int4 NOT NULL, " +
                        "PRIMARY KEY (\"order_id\"), FOREIGN KEY (\"address_id\") REFERENCES \"" + schema + "\".\"addresses\" (\"address_id\"))' \n" +
                "}";

        checkResult(result, expected);
    }

    @After
    public void cleanup() throws SQLException {
        // delete schemas
        try (Connection c = datasource.getConnection()) {
            try (CallableStatement s = c.prepareCall("drop schema " + schema_two + " cascade")) {
                s.execute();
            }

            try (CallableStatement s = c.prepareCall("drop schema " + schema + " cascade")) {
                s.execute();
            }
        }
    }
}
