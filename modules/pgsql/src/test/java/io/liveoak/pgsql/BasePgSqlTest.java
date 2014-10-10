/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceRef;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.pgsql.extension.PgSqlExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.postgresql.ds.PGPoolingDataSource;
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
 */
public class BasePgSqlTest extends AbstractResourceTestCase {

    protected static final Logger log = Logger.getLogger(BasePgSqlTest.class);

    protected static final String BASEPATH = "sqldata";

    protected static PGPoolingDataSource datasource;

    protected static String schema;
    protected static String schema_two;
    private static boolean skipTests;

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss");
    private static DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.S");

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
        try {
            ds.initialize();
        } catch (Exception e) {
            skipTests = true;
            System.out.println("Failed to initialize datasource. Tests will be skipped ...");
            e.printStackTrace();
            return;
        }

        datasource = ds;

        schema = "xlo_test_" + UUID.randomUUID().toString().substring(0, 8);
        schema_two = "xlo_test_" + UUID.randomUUID().toString().substring(0, 8);
        if (schema.compareTo(schema_two) > 0) {
            String tmp = schema;
            schema = schema_two;
            schema_two = tmp;
        }
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
        if (skipTests()) {
            return;
        }
        // delete schemas for the test
        try (Connection c = datasource.getConnection()) {
            try (CallableStatement s = c.prepareCall("drop schema " + schema_two + " cascade")) {
                s.execute();
            }

            try (CallableStatement s = c.prepareCall("drop schema " + schema + " cascade")) {
                s.execute();
            }
        }
    }

    protected static boolean skipTests() {
        return skipTests;
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
        config.putProperty("default-schema", schema);

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
                insertOrder(ps, "014-1003095", parseTime("2014-06-07 15:10:15"), 18990, 1);
                insertOrder(ps, "014-2004096", parseTime("2014-04-02 11:06:12"), 43800, 2);
                insertOrder(ps, "014-2004345", parseTime("2014-06-01 18:06:12"), 32500, 2);
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

    protected RequestContext ctx(String pat) {
        return new RequestContext.Builder()
                .requestAttributes(new DefaultRequestAttributes())
                .returnFields(new DefaultReturnFields(pat))
                .build();
    }

    protected RequestContext ctx(String pat, ResourcePath path) {
        return new RequestContext.Builder()
                .requestAttributes(new DefaultRequestAttributes())
                .resourcePath(path)
                .returnFields(new DefaultReturnFields(pat))
                .build();
    }

    protected Timestamp time(String dt) throws DateTimeParseException {
        return Timestamp.valueOf(LocalDateTime.parse(dt, iso));
    }

    protected static Long parseTime(String dt) throws DateTimeParseException {
        return Timestamp.valueOf(LocalDateTime.parse(dt, dtf)).getTime();
    }

    protected List list(Object... objs) {
        return new ArrayList(Arrays.asList(objs));
    }

    protected void checkResource(ResourceState actual, ResourceState expected) {
        // We could simply do:
        //   assertThat(actual).isEqualTo(expected);
        //
        // But that makes it more difficult to pin down the exact point of difference.
        // Therefore we iterate ourselves ...

        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.uri()).isEqualTo(expected.uri());
        assertThat(actual.getPropertyNames()).isEqualTo(expected.getPropertyNames());
        for (String key: actual.getPropertyNames()) {
            Object exval = expected.getProperty(key);
            Object val = actual.getProperty(key);
            if (exval instanceof ResourceState) {
                assertThat(val).isInstanceOf(DefaultResourceState.class);
                checkResource((ResourceState) val, (ResourceState) exval);
            } else if (exval instanceof List) {
                List exls = (List) exval;
                assertThat(val).isInstanceOf(ArrayList.class);
                checkList((List) val, exls);
            } else {
                assertThat(val).isEqualTo(exval);
            }
        }

        List<ResourceState> exmembers = expected.members();
        List<ResourceState> members = actual.members();
        assertThat(members.size()).isEqualTo(exmembers.size());

        int i = 0;
        for (ResourceState member: members) {
            checkResource(member, exmembers.get(i));
            i++;
        }
    }

    private void checkList(List actual, List expected) {
        assertThat(actual.size()).isEqualTo(expected.size());
        int i = 0;
        for (Object val: actual) {
            Object exval = expected.get(i);
            if (val instanceof ResourceState) {
                assertThat(exval).isInstanceOf(ResourceState.class);
                checkResource((ResourceState) val, (ResourceState) exval);
            } else {
                assertThat(val).isEqualTo(exval);
            }
            i++;
        }
    }

    protected ResourceState resource(String endpoint, Object[] properties, ResourceState... members) throws URISyntaxException {
        ResourcePath path = new ResourcePath(endpoint);
        return resource(path.tail().toString(), path.parent().toString(), properties, members);
    }

    protected ResourceState resource(String id, String parentUri, Object[] properties, ResourceState... members) throws URISyntaxException {
        return resource(id, parentUri, properties, Arrays.asList(members));
    }

    protected ResourceState resource(String id, String parentUri, Object[] properties, List<ResourceState> members) throws URISyntaxException {
        DefaultResourceState state = new DefaultResourceState(id);
        state.uri(new URI(parentUri + "/" + id));
        assertThat(properties.length % 2).isEqualTo(0);
        int count = properties.length / 2;
        for (int i = 0; i < count; i++) {
            String key = (String) properties[2*i];
            Object val = properties[2*i + 1];
            state.putProperty(key, val);
        }
        for (ResourceState resource: members) {
            state.members().add(resource);
        }
        return state;
    }

    protected ResourceState resourceRef(String id, String parent) throws URISyntaxException {
        return resourceRef(parent + "/" + id);
    }

    protected ResourceState resourceRef(String uri) throws URISyntaxException {
        return new DefaultResourceRef(new URI(uri));
    }

    protected List<ResourceState> sorted(Comparator<ResourceState> cmp, ResourceState... members) {
        ArrayList ls = new ArrayList(Arrays.asList(members));
        Collections.sort(ls, cmp);
        return ls;
    }

    protected ResourceState obj(Object ... properties) {
        DefaultResourceState state = new DefaultResourceState();
        assertThat(properties.length % 2).isEqualTo(0);
        int count = properties.length / 2;
        for (int i = 0; i < count; i++) {
            String key = (String) properties[2*i];
            Object val = properties[2*i + 1];
            state.putProperty(key, val);
        }
        return state;
    }
}