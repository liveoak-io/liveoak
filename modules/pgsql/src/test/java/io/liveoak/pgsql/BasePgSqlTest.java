package io.liveoak.pgsql;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.codec.DefaultResourceRef;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.pgsql.extension.PgSqlExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.jdbc2.optional.PoolingDataSource;

/**
 * In order to run this test first prepare a local postgresql instance:
 *
 * initdb -D ~/.liveoak/pgsql/data
 * pg_ctl -D ~/.liveoak/pgsql/data -l logfile start
 * createdb test
 * psql test
 * GRANT CREATE ON DATABASE test TO test
 * CREATE USER ‘test’ createdb PASSWORD ‘test’
 * \q
 */
public class BasePgSqlTest extends AbstractResourceTestCase {

    protected static final Logger log = Logger.getLogger(BasePgSqlTest.class);

    protected static final String BASEPATH = "sqldata";

    protected static PGPoolingDataSource datasource;

    protected static String schema;
    protected static String schema_two;

    protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

    @BeforeClass
    public static void initDriver() throws Exception {
        Class.forName("org.postgresql.Driver");

        PoolingDataSource ds = new PoolingDataSource();
        ds.setServerName(System.getProperty("pgsql.server", "localhost"));
        ds.setPortNumber(Integer.parseInt(System.getProperty("pgsql.port", "5432")));
        ds.setDatabaseName(System.getProperty("pgsql.db", "test"));
        ds.setUser(System.getProperty("pgsql.user", "test"));
        ds.setPassword(System.getProperty("pgsql.password", "test"));
        ds.setMaxConnections(10);
        ds.setInitialConnections(1);
        ds.initialize();
        datasource = ds;

        schema = "lo_test_" + UUID.randomUUID().toString().substring(0, 8);
        schema_two = "lo_test_" + UUID.randomUUID().toString().substring(0, 8);

        // first
        try {
            cleanup();
        } catch (Exception e) {
            // ignore
        }

        // create schema for the test
        try (Connection c = datasource.getConnection()) {
            try (CallableStatement s = c.prepareCall("create schema " + schema)) {
                s.execute();
            }
            try (CallableStatement s = c.prepareCall("create schema " + schema_two)) {
                s.execute();
            }
        }

        createTables();
        insertData();
    }

    @AfterClass
    public static void cleanup() throws SQLException {
        // create schema for the test
        try (Connection c = datasource.getConnection()) {
            try (CallableStatement s = c.prepareCall("drop schema " + schema_two + " cascade")) {
                s.execute();
            }

            try (CallableStatement s = c.prepareCall("drop schema " + schema + " cascade")) {
                s.execute();
            }
        }
    }

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "pgsql", new PgSqlExtension(), JsonNodeFactory.instance.objectNode() );
        installResource( "pgsql", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() {
        String server = datasource.getServerName();
        int port = datasource.getPortNumber();
        String db = datasource.getDatabaseName();
        String user = datasource.getUser();
        String password = datasource.getPassword();
        int maxConnections = datasource.getMaxConnections();
        int initialConnections = datasource.getInitialConnections();

        log.debug("Using PostgreSQL on " + server + ":" + port + ", database: " + db + " with user: " + user);

        ResourceState config = new DefaultResourceState();
        config.putProperty("server", server);
        config.putProperty("port", port);
        config.putProperty("db", db);
        config.putProperty("user", user);
        config.putProperty("password", password);
        config.putProperty("max-connections", maxConnections);
        config.putProperty("initial-connections", initialConnections);
        config.putProperty("schemas", Arrays.asList( new String[] { schema, schema_two } ));

        return config;
    }

    public static void createTables() throws Exception {
        try (Connection c = datasource.getConnection()) {

            try (PreparedStatement ps = c.prepareStatement("create table " + schema + ".addresses (" +
                    "address_id integer PRIMARY KEY, " +
                    "name varchar (255) NOT NULL, " +
                    "street varchar (255) NOT NULL, " +
                    "postcode varchar (10), " +
                    "city varchar (60) NOT NULL, " +
                    "country_iso char(2), " +
                    "is_company boolean default false)")) {
                ps.execute();
            }

            try (PreparedStatement ps = c.prepareStatement("create table " + schema + ".orders (" +
                    "order_id varchar (40) PRIMARY KEY, " +
                    "create_date timestamp NOT NULL, " +
                    "total int8 NOT NULL, " +
                    "address_id integer NOT NULL, " +
                    "FOREIGN KEY (address_id) REFERENCES " + schema + ".addresses (address_id))")) {
                ps.execute();
            }

            try (PreparedStatement ps = c.prepareStatement("create table " + schema_two + ".orders (" +
                    "order_id varchar (40) PRIMARY KEY, " +
                    "create_date timestamp NOT NULL, " +
                    "total int8 NOT NULL, " +
                    "address_id integer NOT NULL, " +
                    "FOREIGN KEY (address_id) REFERENCES " + schema + ".addresses (address_id))")) {
                ps.execute();
            }
        }
    }

    public static void insertData() throws Exception {
        try (Connection c = datasource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("insert into " + schema + ".addresses VALUES (?,?,?,?,?,?,?)")) {
                insertAddress(ps, 1, "John F. Doe", "Liveoak street 7", null, "London", "UK", false);
                insertAddress(ps, 2, "Lombaas Inc.", "Liveoak square 1", "94114", "San Francisco", "US", true);
            }

            try (PreparedStatement ps = c.prepareStatement("insert into " + schema_two + ".orders VALUES (?,?,?,?)")) {
                insertOrder(ps, "014-1003095", sdf.parse("2014-06-07 15:10:15").getTime(), 18990, 1);
                insertOrder(ps, "014-2004096", sdf.parse("2014-04-02 11:06:12").getTime(), 43800, 2);
                insertOrder(ps, "014-2004345", sdf.parse("2014-06-01 18:06:12").getTime(), 32500, 2);
            }
        }
    }

    protected static void insertAddress(PreparedStatement ps, int id, String name, String street, String zip, String city, String country, boolean inc) throws SQLException {
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, street);
        ps.setString(4, zip);
        ps.setString(5, city);
        ps.setString(6, country);
        ps.setBoolean(7, inc);
        ps.execute();
    }

    protected static void insertOrder(PreparedStatement ps, String id, long createTime, int total, int fk) throws SQLException {
        ps.setString(1, id);
        ps.setTimestamp(2, new Timestamp(createTime));
        ps.setInt(3, total);
        ps.setInt(4, fk);
        ps.execute();
    }

    protected ResourceState resourceRef(String id, String parent) throws URISyntaxException {
        return resourceRef(parent + "/" + id);
    }

    protected ResourceState resourceRef(String uri) throws URISyntaxException {
        return new DefaultResourceRef(new URI(uri));
    }
}