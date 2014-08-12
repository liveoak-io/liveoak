package io.liveoak.pgsql;

import java.io.IOException;
import java.util.UUID;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class TestHelper extends BasePgSqlHttpTest {

    private static final String APP = "pgsql-demo";
    private static final String RESOURCE = "sqldata";
    private static final String ENDPOINT = "http://localhost:8080/" + APP + "/" + RESOURCE;
    private static final String schema = "test";

    private final static String ALICE = "00000000001";
    private final static String BOB = "00000000002";
    private final static String WONDERLAND = "00000000001";

    @Override
    public void setUpSystem() {
        // don't setup an embedded server - this helper is to be used against standalone LiveOak setup
    }

    @Override
    public void tearDownSystem() {
        // no setup - no teardown
    }

    @Test
    public void createUsersTable() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'users',                                                    \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'user_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'nick',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 60,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'last_login',                                        \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['user_id']                                        \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': 'users;schema',                                             \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/users;schema'           \n" +
                "   },                                                               \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'user_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'nick',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 60,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'last_login',                                        \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'size': 29,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['user_id'],                                       \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"users\" (\"user_id\" varchar (40), \"nick\" varchar (60) UNIQUE NOT NULL, " +
                "\"last_login\" timestamp NOT NULL, PRIMARY KEY (\"user_id\"))'       \n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createRoomsTable() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'rooms',                                                    \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'room_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'owner_id',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 60,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'public',                                            \n" +
                "       'type': 'boolean',                                           \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_time',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['room_id'],                                       \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': 'users',                                             \n" +
                "      'columns': ['owner_id']                                       \n" +
                "   }]                                                               \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': 'rooms;schema',                                             \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/rooms;schema'           \n" +
                "   },                                                               \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'room_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'owner_id',                                          \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'name',                                              \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 60,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'public',                                            \n" +
                "       'type': 'bool',                                              \n" +
                "       'size': 1 ,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_time',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'size': 29,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['room_id'],                                       \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': '" + schema + ".users',                              \n" +
                "      'columns': ['owner_id']                                       \n" +
                "   }],                                                              \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"rooms\" (\"room_id\" varchar (40), \"owner_id\" varchar (40) NOT NULL, " +
                "\"name\" varchar (60) UNIQUE NOT NULL, \"public\" bool NOT NULL, \"create_time\" timestamp NOT NULL, PRIMARY KEY (\"room_id\"), " +
                "FOREIGN KEY (\"owner_id\") REFERENCES \"" + schema + "\".\"users\" (\"user_id\"))'\n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createMessagesTable() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': 'messages',                                                 \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'message_id',                                        \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'user_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'room_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40                                                   \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_time',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'nullable': false                                            \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'content',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false                                            \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['message_id'],                                    \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': 'users',                                             \n" +
                "      'columns': ['user_id']                                        \n" +
                "   }, {                                                             \n" +
                "      'table': 'rooms',                                             \n" +
                "      'columns': ['room_id']                                        \n" +
                "   }]                                                               \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': 'messages;schema',                                          \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/messages;schema'        \n" +
                "   },                                                               \n" +
                "  'columns': [                                                      \n" +
                "     {                                                              \n" +
                "       'name': 'message_id',                                        \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': true                                               \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'user_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'room_id',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 40,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'create_time',                                       \n" +
                "       'type': 'timestamp',                                         \n" +
                "       'size': 29,                                                  \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     },                                                             \n" +
                "     {                                                              \n" +
                "       'name': 'content',                                           \n" +
                "       'type': 'varchar',                                           \n" +
                "       'size': 255,                                                 \n" +
                "       'nullable': false,                                           \n" +
                "       'unique': false                                              \n" +
                "     }],                                                            \n" +
                "  'primary-key': ['message_id'],                                    \n" +
                "  'foreign-keys': [{                                                \n" +
                "      'table': '" + schema + ".rooms',                              \n" +
                "      'columns': ['room_id']                                        \n" +
                "   }, {                                                             \n" +
                "      'table': '" + schema + ".users',                              \n" +
                "      'columns': ['user_id']                                        \n" +
                "   }],                                                              \n" +
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"messages\" (\"message_id\" varchar (40), \"user_id\" varchar (40) NOT NULL, " +
                "\"room_id\" varchar (40) NOT NULL, \"create_time\" timestamp NOT NULL, \"content\" varchar (255) NOT NULL, PRIMARY KEY (\"message_id\"), " +
                "FOREIGN KEY (\"room_id\") REFERENCES \"" + schema + "\".\"rooms\" (\"room_id\"), " +
                "FOREIGN KEY (\"user_id\") REFERENCES \"" + schema + "\".\"users\" (\"user_id\"))'\n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createUserAlice() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT + "/users");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': '00000000001',                                              \n" +
                "  'nick': 'alice',                                                  \n" +
                "  'last_login': 0                                                   \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : '00000000001',                                             \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/users/00000000001'      \n" +
                "  },                                                                \n" +
                "  'user_id' : '00000000001',                                        \n" +
                "  'nick' : 'alice',                                                 \n" +
                "  'last_login' : 0,                                                 \n" +
                "  'messages' : [ ],                                                 \n" +
                "  'rooms' : [ ]                                                     \n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createUserBob() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT + "/users");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String json = "{                                                             \n" +
                "  'id': '00000000002',                                              \n" +
                "  'nick': 'bob',                                                    \n" +
                "  'last_login': 0                                                   \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : '00000000002',                                             \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/users/00000000002'      \n" +
                "  },                                                                \n" +
                "  'user_id' : '00000000002',                                        \n" +
                "  'nick' : 'bob',                                                   \n" +
                "  'last_login' : 0,                                                 \n" +
                "  'messages' : [ ],                                                 \n" +
                "  'rooms' : [ ]                                                     \n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createRoomWonderland() throws IOException {
        HttpPost post = new HttpPost(ENDPOINT + "/rooms");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);


        String json = "{                                                             \n" +
                "  'id': '00000000001',                                              \n" +
                "  'owner': {                                                        \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/users/00000000002'     \n" +
                "    }                                                               \n" +
                "  },                                                                \n" +
                "  'name': 'Wonderland',                                             \n" +
                "  'public': true,                                                   \n" +
                "  'create_time': '2014-08-10T22:22:22'                              \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : '00000000001',                                             \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "/rooms/00000000001'      \n" +
                "  },                                                                \n" +
                "  'room_id' : '00000000001',                                        \n" +
                "  'name': 'Wonderland',                                             \n" +
                "  'public': true,                                                   \n" +
                "  'create_time': 1407702142000,                                     \n" +
                "  'messages' : [ ],                                                 \n" +
                "  'owner': {                                                        \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/users/00000000002'     \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        checkResult(result, expected);
    }

    @Test
    public void createAliceToWonderlandHelloMessage() throws IOException {
        sendMessage(ALICE, WONDERLAND, "Hello, anybody here?");
    }

    @Test
    public void createBobToWonderlandHelloMessage() throws IOException {
        sendMessage(BOB, WONDERLAND, "Hi, it\\'s just me ...");
    }

    @Test
    public void createAliceBlurbToWonderlandMessage() throws IOException {
        sendMessage(ALICE, WONDERLAND, "Now what?");
    }

    @Test
    public void createBobBlurbToWonderlandMessage() throws IOException {
        sendMessage(BOB, WONDERLAND, "someone\\'s at the door ... brb");
    }


    private void sendMessage(String from, String toRoom, String message) throws IOException {
        HttpPost post = new HttpPost(ENDPOINT + "/messages");
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String id = UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();

        String json = "{                                                             \n" +
                "  'id': '" + id + "',                                               \n" +
                "  'user': {                                                         \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/users/" + from + "'    \n" +
                "    }                                                               \n" +
                "  },                                                                \n" +
                "  'room': {                                                         \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/rooms/" + toRoom + "'  \n" +
                "    }                                                               \n" +
                "  },                                                                \n" +
                "  'create_time': " + now + ",                                       \n" +
                "  'content': '" + message + "'                                      \n" +
                "}";

        String result = postRequest(post, json);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id': '" + id + "',                                               \n" +
                "  'self': {                                                         \n" +
                "    'href': '/" + APP + "/" + RESOURCE + "/messages/" + id + "'    \n" +
                "  },                                                                \n" +
                "  'message_id': '" + id + "',                                      \n" +
                "  'create_time': " + now + ",                                       \n" +
                "  'content': '" + message + "',                                     \n" +
                "  'user': {                                                         \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/users/" + from + "'    \n" +
                "    }                                                               \n" +
                "  },                                                                \n" +
                "  'room': {                                                         \n" +
                "    'self': {                                                       \n" +
                "      'href': '/" + APP + "/" + RESOURCE + "/rooms/" + toRoom + "'  \n" +
                "    }                                                               \n" +
                "  }                                                                 \n" +
                "}";

        checkResult(result, expected);

    }

    @Test
    public void createAllTables() throws IOException {
        createUsersTable();
        createRoomsTable();
        createMessagesTable();
    }

    @Test
    public void fillWithData() throws IOException {
        createUserAlice();
        createUserBob();
        createRoomWonderland();
        createAliceToWonderlandHelloMessage();
        createBobToWonderlandHelloMessage();
    }

    @Test
    public void dropAllTables() throws IOException {
        HttpGet get = new HttpGet(ENDPOINT);
        get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String result = getRequest(get);
        System.out.println(result);

        HttpPost post = new HttpPost(ENDPOINT + "/_batch?action=delete");
        post.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        result = postRequest(post, result);
        System.out.println(result);

        // do another GET and make sure there are now zero collections
        result = getRequest(get);
        System.out.println(result);

        String expected = "{                                                         \n" +
                "  'id' : 'sqldata',                                                 \n" +
                "  'self' : {                                                        \n" +
                "    'href' : '/" + APP + "/" + RESOURCE + "'                        \n" +
                "  },                                                                \n" +
                "  'count' : 1,                                                      \n" +
                "  'type' : 'database',                                              \n" +
                "  '_members' : [ {                                                  \n" +
                "    'id' : '_batch',                                                \n" +
                "    'self' : {                                                      \n" +
                "      'href' : '/" + APP + "/" + RESOURCE + "/_batch'               \n" +
                "    }                                                               \n" +
                "  } ]                                                               \n" +
                "}";

        checkResult(result, expected);
    }
}
