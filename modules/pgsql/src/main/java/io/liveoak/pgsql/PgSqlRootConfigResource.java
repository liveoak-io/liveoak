package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.ForeignKey;
import io.liveoak.pgsql.meta.PrimaryKey;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.pgsql.meta.TableRef;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.postgresql.ds.PGPoolingDataSource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootConfigResource extends DefaultRootResource {

    private static Logger log = Logger.getLogger(PgSqlRootConfigResource.class);
    private PGPoolingDataSource ds;
    private Catalog catalog;
    private List<String> exposedSchemas;
    private List<String> blockedSchemas;
    private String defaultSchema;

    public PgSqlRootConfigResource(String id) {
        super(id);
    }

    @Override
    public void stop() {
        PGPoolingDataSource ds = this.ds;
        this.ds = null;

        if ( ds != null ) {
            ds.close();
        }
    }

    /**
     * Users of the connection must make sure to call {@link java.sql.Connection#close()} when done using it,
     * so that it is returned to the pool.
     *
     * @return a connection retrieved from the pool
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (ds == null) {
            throw new IllegalStateException("DataSource not available");
        }
        return ds.getConnection();
    }

    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        PGPoolingDataSource ds = this.ds;
        sink.accept("server", ds.getServerName() );
        sink.accept("port", ds.getPortNumber());
        sink.accept("db", ds.getDatabaseName());
        sink.accept("user", ds.getUser());
        sink.accept("password", ds.getPassword());
        sink.accept("max-connections", ds.getMaxConnections());
        sink.accept("initial-connections", ds.getInitialConnections());
        if (exposedSchemas != null && exposedSchemas.size() > 0) {
            sink.accept("schemas", exposedSchemas);
        }
        if (blockedSchemas != null && blockedSchemas.size() > 0) {
            sink.accept("blocked-schemas", blockedSchemas);
        }
        if (defaultSchema != null) {
            sink.accept("default-schema", defaultSchema);
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        String dbName = state.getPropertyAsString("db");
        if (dbName == null) {
            throw new InitializationException("Database name not specified! Use \"db\": \"DATABASE_NAME\"");
        }

        String server = state.getPropertyAsString("server");
        if (server == null) {
            server = "localhost";
        }

        Integer port = state.getPropertyAsInteger("port");
        if (port == null) {
            port = 5432;
        }

        String user = state.getPropertyAsString("user");
        String pass = state.getPropertyAsString("password");

        Integer maxConnections = state.getPropertyAsInteger("max-connections");
        if (maxConnections == null) {
            maxConnections = 10;
        }

        Integer initialConnections = state.getPropertyAsInteger("initial-connections");
        if (initialConnections == null) {
            initialConnections = 1;
        }

        List<String> exposedSchemas = (List<String>) state.getProperty("schemas");
        if (exposedSchemas != null) {
            this.exposedSchemas = exposedSchemas;
        }

        List<String> blockedSchemas = (List<String>) state.getProperty("blocked-schemas");
        if (blockedSchemas != null) {
            this.blockedSchemas = blockedSchemas;
        }

        this.defaultSchema = state.getPropertyAsString("default-schema");

        PGPoolingDataSource old = this.ds;
        boolean recreate = old == null
                || !dbName.equals(old.getDatabaseName())
                || !server.equals(old.getServerName())
                || !port.equals(old.getPortNumber())
                || !user.equals(old.getUser())
                || !pass.equals(old.getPassword())
                || !maxConnections.equals(old.getMaxConnections())
                || !initialConnections.equals(old.getInitialConnections());

        if (recreate) {
            // reinit ds with new settings
            PGPoolingDataSource nu = new PGPoolingDataSource();
            nu.setServerName(server);
            nu.setPortNumber(port);
            nu.setDatabaseName(dbName);
            nu.setUser(user);
            nu.setPassword(pass);
            nu.setMaxConnections(maxConnections);
            nu.setInitialConnections(initialConnections);
            nu.initialize();

            this.ds = nu;

            if (old != null) {
                try {
                    old.close();
                } catch (Exception e) {
                    log.debug("[IGNORED] Exception while closing the datasource: ", e);
                }
            }

            reloadSchema();
        }

        responder.resourceUpdated(this);
    }

    private Set<String> calculateEffectiveSchemas(Connection c, String catalog, List<String> exposedSchemas, List<String> blockedSchemas) {
        List<String> schemas = new LinkedList<>();
        try (ResultSet rs = c.getMetaData().getSchemas(catalog, null)) {
            while (rs.next()) {
                schemas.add(rs.getString("table_schem"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Set<String> effectiveSchemas = new HashSet<>();

        if (exposedSchemas != null && exposedSchemas.size() > 0) {
            for (String schema: schemas) {
                if (exposedSchemas.contains(schema)) {
                    effectiveSchemas.add(schema);
                }
            }
        } else {
            effectiveSchemas.addAll(schemas);
        }

        if (blockedSchemas != null && blockedSchemas.size() > 0) {
            effectiveSchemas.removeAll(blockedSchemas);
        }

        return effectiveSchemas;
    }

    public String getDBName() {
        return ds != null ? ds.getDatabaseName() : null;
    }

    private String determineDefaultSchema(String user, Set<String> schemas) throws SQLException {
        String schema = schemas.contains(user) ? user : schemas.contains("public") ? "public" : null;

        if (schema == null) {
            throw new RuntimeException("Could not determine default schema - specify 'default-schema' in config JSON");
        }
        return schema;
    }

    public void reloadSchema() throws SQLException {
        try (Connection c = getConnection()) {
            Set<String> schemas = calculateEffectiveSchemas(c, ds.getDatabaseName(), exposedSchemas, blockedSchemas);
            if (defaultSchema == null) {
                defaultSchema = determineDefaultSchema(ds.getUser(), schemas);
            }
            Map<TableRef, Table> tables = reverseEngineerTableInfo(c, ds.getDatabaseName(), schemas);
            catalog = new Catalog(schemas, defaultSchema, tables);
        }
    }

    private static Map<TableRef, Table> reverseEngineerTableInfo(Connection c, String catalog, Set<String> schemas) throws SQLException {

        HashMap<TableRef, Table> tables = new HashMap<>();

        try (ResultSet rs = c.getMetaData().getTables(catalog, null, null, new String[] {"TABLE"})) {
            while (rs.next()) {
                String schema = rs.getString("table_schem");
                String table = rs.getString("table_name");

                // check for allowed schemas
                if (!schemas.contains(schema)) {
                    continue;
                }

                HashSet<String> uniques = new HashSet<>();
                try (ResultSet idxrs = c.getMetaData().getIndexInfo(catalog, schema, table, false, true)) {
                    while (idxrs.next()) {
                        if (!idxrs.getBoolean("non_unique")) {
                            uniques.add(idxrs.getString("column_name"));
                        }
                    }
                }

                List<Column> cols = new LinkedList<>();
                try (ResultSet colrs = c.getMetaData().getColumns(catalog, schema, table, null)) {
                    while (colrs.next()) {
                        String name = colrs.getString("column_name");
                        String type = colrs.getString("type_name");
                        int size = colrs.getInt("column_size");
                        boolean isNullable = colrs.getBoolean("is_nullable");

                        Column col = new Column(new TableRef(schema, table), name, type, size, !isNullable, uniques.contains(name));
                        cols.add(col);
                    }
                }

                PrimaryKey pk = null;
                List<Column> pkcols = new LinkedList<>();
                try (ResultSet idxrs = c.getMetaData().getBestRowIdentifier(catalog, schema, table, DatabaseMetaData.bestRowUnknown, false)) {
                    while (idxrs.next()) {
                        String colName = idxrs.getString("column_name");
                        for (Column col : cols) {
                            if (col.name().equals(colName)) {
                                pkcols.add(col);
                                break;
                            }
                            throw new RuntimeException("Assertion failed - PK not a known table column: " + colName + " on " + table);
                        }
                    }
                }
                if (pkcols.size() > 0) {
                    pk = new PrimaryKey(pkcols);
                }

                List<ForeignKey> fks = new LinkedList<>();

                try (ResultSet fkrs = c.getMetaData().getCrossReference(null, null, null, catalog, schema, table)) {
                    while (fkrs.next()) {
                        String colName = fkrs.getString("fkcolumn_name");
                        String refschema = fkrs.getString("pktable_schem");
                        String reftable = fkrs.getString("pktable_name");
                        List<Column> fkcols = new LinkedList<>();
                        for (Column col : cols) {
                            if (col.name().equals(colName)) {
                                fkcols.add(col);
                                break;
                            }
                        }
                        if (fkcols.size() == 0) {
                            throw new RuntimeException("Assertion failed - FK not a known table column: " + colName + " on " + table);
                        }
                        fks.add(new ForeignKey(fkcols, new TableRef(refschema, reftable)));
                    }
                }

                Table tableMeta = new Table(schema, table, cols, pk, fks);
                tables.put(new TableRef(schema, table), tableMeta);
            }
        }

        return tables;
    }
}
