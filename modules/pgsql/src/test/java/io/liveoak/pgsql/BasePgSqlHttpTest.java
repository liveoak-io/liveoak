/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.pgsql.extension.PgSqlExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.fest.assertions.Assertions;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.postgresql.jdbc2.optional.PoolingDataSource;

import static org.fest.assertions.Assertions.assertThat;

/**
 * In order to run this test first prepare a local postgresql instance:
 *
 * initdb -D ~/.liveoak/pgsql/data
 * pg_ctl -D ~/.liveoak/pgsql/data -l logfile start
 * createdb test
 * psql test
 * CREATE USER test createdb PASSWORD 'test';
 * GRANT CREATE ON DATABASE test TO test;
 * \q
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class BasePgSqlHttpTest extends AbstractHTTPResourceTestCase {

    private static final Logger log = Logger.getLogger(BasePgSqlHttpTest.class);

    protected static final String APPLICATION_JSON = "application/json";
    protected static final String BASEPATH = "sqldata";
    protected static final JsonFactory JSON_FACTORY = new JsonFactory();

    protected String schema;
    protected String schema_two;
    protected PoolingDataSource datasource;
    private boolean skipTests;

    static {
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    protected JsonNode parseJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper(JSON_FACTORY);
        JsonParser jp = JSON_FACTORY.createParser(jsonString);
        return mapper.readTree(jp);
    }


    protected void checkResult(String result, String expected) throws IOException {
        JsonNode resultNode = parseJson(result);
        JsonNode expectedNode = parseJson(expected);

        assertThat((Object) resultNode).isEqualTo(expectedNode);
    }

    protected void checkResultForError(String result) throws IOException {
        JsonNode node = parseJson(result);
        JsonNode cause = node.get("cause");
        if (node.get("error-type") != null) {
            Assert.fail("Server returned an error: error-type: " + node.get("error-type") + ", cause: " + cause);
        }
    }

    protected void expectError(String result, String errorType) throws IOException {
        JsonNode node = parseJson(result);
        String error = node.get("error-type").asText();
        Assertions.assertThat(error).isNotNull();
        Assertions.assertThat(error).isEqualTo(errorType);
    }

    protected String postRequest(HttpPost post, String json) throws IOException {

        StringEntity entity = new StringEntity(json, ContentType.create(APPLICATION_JSON, "UTF-8"));
        post.setEntity(entity);

        System.err.println("DO POST - " + post.getURI());
        System.out.println("\n" + json);

        CloseableHttpResponse result = httpClient.execute(post);

        System.err.println("=============>>>");
        System.err.println(result);

        HttpEntity resultEntity = result.getEntity();

        assertThat(resultEntity.getContentLength()).isGreaterThan(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultEntity.writeTo(baos);

        String resultStr = new String(baos.toByteArray());
        System.err.println(resultStr);
        System.err.println("\n<<<=============");
        return resultStr;
    }

    protected String putRequest(HttpPut put, String json) throws IOException {

        StringEntity entity = new StringEntity(json, ContentType.create(APPLICATION_JSON, "UTF-8"));
        put.setEntity(entity);

        System.err.println("DO PUT - " + put.getURI());
        System.out.println("\n" + json);

        CloseableHttpResponse result = httpClient.execute(put);

        System.err.println("=============>>>");
        System.err.println(result);

        HttpEntity resultEntity = result.getEntity();

        assertThat(resultEntity.getContentLength()).isGreaterThan(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultEntity.writeTo(baos);

        String resultStr = new String(baos.toByteArray());
        System.err.println(resultStr);
        System.err.println("\n<<<=============");
        return resultStr;
    }

    protected String getRequest(HttpGet get) throws IOException {
        System.err.println("DO GET - " + get.getURI());
        return request(get);
    }

    protected String deleteRequest(HttpDelete delete) throws IOException {
        System.err.println("DO DELETE - " + delete.getURI());
        return request(delete);
    }

    protected String request(HttpRequestBase request) throws IOException {
        CloseableHttpResponse result = httpClient.execute(request);

        System.err.println("=============>>>");
        System.err.println(result);

        HttpEntity resultEntity = result.getEntity();

        assertThat(resultEntity.getContentLength()).isGreaterThan(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resultEntity.writeTo(baos);

        String resultStr = new String(baos.toByteArray());
        System.err.println(resultStr);
        System.err.println("\n<<<=============");
        return resultStr;
    }

    @Override
    public void loadExtensions() throws Exception {

        schema = "xlo_test_" + UUID.randomUUID().toString().substring(0, 8);
        schema_two = "xlo_test_" + UUID.randomUUID().toString().substring(0, 8);
        if (schema.compareTo(schema_two) > 0) {
            String tmp = schema;
            schema = schema_two;
            schema_two = tmp;
        }
        loadExtension( "pgsql", new PgSqlExtension(), JsonNodeFactory.instance.objectNode() );
        installResource( "pgsql", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() throws SQLException, ClassNotFoundException {

        String server = System.getProperty("pgsql.server", "localhost");
        int port = Integer.parseInt(System.getProperty("pgsql.port", "5432"));
        String db = System.getProperty("pgsql.db", "test");
        String user = System.getProperty("pgsql.user", "test");
        String password = System.getProperty("pgsql.password", "test");
        int maxConnections = 10;
        int initialConnections = 1;
        boolean canCreateSchema = Boolean.valueOf(System.getProperty("pgsql.allow_create_schema", "true"));

        log.debug("Using PostgreSQL on " + server + ":" + port + ", database: " + db + " with user: " + user);

        setupDataSource(server, port, db, user, password, maxConnections, initialConnections);

        ResourceState config = new DefaultResourceState();
        config.putProperty("server", server);
        config.putProperty("port", port);
        config.putProperty("db", db);
        config.putProperty("user", user);
        config.putProperty("password", password);
        config.putProperty("max-connections", maxConnections);
        config.putProperty("initial-connections", initialConnections);
        config.putProperty("schemas", Arrays.asList(new String[]{schema, schema_two}));
        config.putProperty("default-schema", schema);
        config.putProperty("allow-create-schema", canCreateSchema);

        return config;
    }

    private void setupDataSource(String server, int port, String db, String user, String password, int maxConnections, int initialConnections) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        PoolingDataSource ds = new PoolingDataSource();
        ds.setServerName(server);
        ds.setPortNumber(port);
        ds.setDatabaseName(db);
        ds.setUser(user);
        ds.setPassword(password);
        ds.setMaxConnections(maxConnections);
        ds.setInitialConnections(initialConnections);
        try {
            ds.initialize();
        } catch (Exception e) {
            skipTests = true;
            System.out.println("Failed to initialize datasource. Tests will be skipped ...");
            e.printStackTrace();
        }

        datasource = ds;
    }

    protected boolean skipTests() {
        return skipTests;
    }
}
