package io.liveoak.pgsql;

import java.io.IOException;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class TestHelper extends BasePgSqlHttpTest {

    private static final String ENDPOINT = "http://localhost:8080/demoApp/sqldata";
    private static final String schema = "test";

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
                "       'nullable': false                                            \n" +
                "       'unique': true,                                              \n" +
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
                "    'href' : '/testApp/sqldata/users;schema'                        \n" +
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
                "  'ddl' : 'CREATE TABLE \"" + schema + "\".\"users\" ( \"user_id\" varchar (40),\"nick\" varchar (60) UNIQUE NOT NULL," +
                "\"last_login\" timestamp NOT NULL, PRIMARY KEY (\"user_id\")'       \n" +
                "}";

        checkResult(result, expected);
    }
}
