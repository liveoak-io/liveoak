package io.liveoak.pgsql;

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
import org.jboss.logging.Logger;
import org.postgresql.jdbc2.optional.PoolingDataSource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class BasePgSqlHttpTest extends AbstractHTTPResourceTestCase {

    private static final Logger log = Logger.getLogger(BasePgSqlHttpTest.class);

    protected static final String BASEPATH = "sqldata";
    protected static final JsonFactory JSON_FACTORY = new JsonFactory();

    protected String schema;
    protected String schema_two;
    protected PoolingDataSource datasource;

    static {
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    protected JsonNode parseJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper(JSON_FACTORY);
        JsonParser jp = JSON_FACTORY.createParser(jsonString);
        return mapper.readTree(jp);
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
        ds.initialize();
        datasource = ds;
    }
}
