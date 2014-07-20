package io.liveoak.pgsql;

import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.pgsql.extension.PgSqlExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class BasePgSqlHttpTest extends AbstractHTTPResourceTestCase {

    private static final Logger log = Logger.getLogger(BasePgSqlHttpTest.class);

    protected static final String BASEPATH = "sqldata";

    protected String schema;
    protected String schema_two;

    @Override
    public void loadExtensions() throws Exception {

        schema = "lo_test_" + UUID.randomUUID().toString().substring(0, 8);
        schema_two = "lo_test_" + UUID.randomUUID().toString().substring(0, 8);

        loadExtension( "pgsql", new PgSqlExtension(), JsonNodeFactory.instance.objectNode() );
        installResource( "pgsql", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() {

        String server = System.getProperty("pgsql.server", "localhost");
        int port = Integer.parseInt(System.getProperty("pgsql.port", "5432"));
        String db = System.getProperty("pgsql.db", "test");
        String user = System.getProperty("pgsql.user", "test");
        String password = System.getProperty("pgsql.password", "test");
        int maxConnections = 10;
        int initialConnections = 1;
        boolean canCreateSchema = Boolean.valueOf(System.getProperty("pgsql.allow_create_schema", "true"));

        log.debug("Using PostgreSQL on " + server + ":" + port + ", database: " + db + " with user: " + user);

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
}
