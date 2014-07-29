package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRootResource extends DefaultRootResource {

    private static final String TABLE_NAMES = "pg.table.names";

    private static final String BATCH_ENDPOINT = "_batch";

    private final PgSqlRootConfigResource configResource;

    public PgSqlRootResource(String id) {
        super(id);
        configResource = new PgSqlRootConfigResource(id);
    }

    public PgSqlRootConfigResource configuration() {
        return configResource;
    }

    /**
     * Users of the connection must make sure to call {@link java.sql.Connection#close()} when done using it,
     * so that it is returned to the pool.
     *
     * @return a connection retrieved from the pool
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return configResource.getConnection();
    }

    public Catalog getCatalog() {
        return configResource.getCatalog();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        // determine table names
        List<String> tables = getCatalog().tableIds();

        // store to ctx attributes to pass on to readMembers
        ctx.requestAttributes().setAttribute(TABLE_NAMES, tables);

        // here only set num of tables as size
        sink.accept("count", tables.size() + 1);

        // maybe some other things to do with db as a whole
        sink.accept("type", "database");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(new PgSqlBatchResource(this, BATCH_ENDPOINT));
        List<String> tables = (List<String>) ctx.requestAttributes().getAttribute(TABLE_NAMES);
        for (String table: tables) {
            sink.accept(new PgSqlTableResource(this, table));
        }
        sink.close();
    }

    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (BATCH_ENDPOINT.equals(id)) {
            responder.resourceRead(new PgSqlBatchResource(this, BATCH_ENDPOINT));
            return;
        }

        String tableId = id;
        boolean schemaReq = false;

        ResourcePath.Segment tail = ctx.resourcePath() != null ? ctx.resourcePath().tail() : null;
        if (tail != null && tail.matrixParameters().containsKey("schema")) {
            tableId = tail.name();
            schemaReq = true;
        }
        List<String> tables = getCatalog().tableIds();
        int pos = tables.indexOf(tableId);
        if (pos == -1) {
            responder.noSuchResource(tableId);
        } else if (schemaReq) {
            responder.resourceRead(new PgSqlTableSchemaResource(this, tail.name()));
        } else {
            responder.resourceRead(new PgSqlTableResource(this, tables.get(pos)));
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        String id = state.id();
        if (id == null || id.length() == 0) {
            responder.invalidRequest("No id");
            return;
        }

        // remove any matrix parameters if present
        // (response to /app/pgsql/table;schema for example produces 'id': 'table;schema' in the output,
        // and we want to properly process such response JSON as POST input)
        id = new ResourcePath(id).tail().toString();

        TableRef tableRef = new TableRef(id);

        // check if table exists
        Table table = getCatalog().table(tableRef);
        if (table != null) {
            responder.resourceAlreadyExists(id);
            return;
        }

        // check if schema is exposed
        String schema = tableRef.schema();
        if (schema == null) {
            schema = getCatalog().defaultSchema();
            tableRef = new TableRef(schema, tableRef.name());
        }

        Set<String> seenNames = new HashSet<>();
        List<Column> cols = new LinkedList<>();
        List columns = state.getPropertyAsList("columns");
        if (columns == null) {
            responder.invalidRequest("Invalid JSON message - 'columns' missing");
            return;
        }
        for (Object o: columns) {
            if (o instanceof ResourceState == false) {
                responder.invalidRequest("Invalid JSON message - 'columns' item not an object: " + o);
                return;
            }
            Column col = column(tableRef, (ResourceState) o);
            seenNames.add(col.name());
            cols.add(col);
        }

        PrimaryKey pk = null;
        List pkCols = state.getPropertyAsList("primary-key");
        if (pkCols != null) {
            List<Column> pkColumns = new LinkedList<>();

            for (Object pkCol: pkCols) {
                if (pkCol instanceof String == false) {
                    responder.invalidRequest("Invalid JSON message - 'primary-key' item not a string: " + pkCol);
                    return;
                }
                if (!seenNames.contains(pkCol)) {
                    responder.invalidRequest("Invalid JSON message - 'primary-key' refers to non-existent column: " + pkCol);
                    return;
                }
                for (Column col: cols) {
                    if (col.name().equals(pkCol)) {
                        pkColumns.add(col);
                        break;
                    }
                }
            }
            pk = new PrimaryKey(pkColumns);
        } else {
            responder.invalidRequest("Invalid JSON message - 'primary-key' field missing");
            return;
        }

        List<ForeignKey> fks = new LinkedList<>();
        List fkList = state.getPropertyAsList("foreign-keys");

        if (fkList != null) {
            for (Object o: fkList) {
                if (o instanceof ResourceState == false) {
                    responder.invalidRequest("Invalid JSON message - 'foreign-keys' item not an object: " + o);
                    return;
                }
                ResourceState item = (ResourceState) o;
                String fkTableId = item.getPropertyAsString("table");
                if (fkTableId == null) {
                    responder.invalidRequest("Invalid JSON message - 'table' property missing on 'foreign-keys' item: " + o);
                    return;
                }

                TableRef fkTableRef = new TableRef(fkTableId);
                // make sure the referred table exists
                Table fkTable = getCatalog().table(fkTableRef);
                if (fkTable == null) {
                    responder.invalidRequest("Table referred to by 'foreign-keys' item does not exist or is not visible: " + fkTableId);
                    return;
                }

                List fkcols = item.getPropertyAsList("columns");
                if (fkcols == null) {
                    responder.invalidRequest("Invalid JSON message - 'columns' property missing on 'foreign-keys' item: " + o);
                    return;
                }

                List<Column> fkColumns = new LinkedList<>();
                for (Object fkcol: fkcols) {
                    if (fkcol instanceof String == false) {
                        responder.invalidRequest("Invalid JSON message - 'foreign-keys' / 'columns' item not a string: " + fkcol);
                        return;
                    }
                    if (!seenNames.contains(fkcol)) {
                        responder.invalidRequest("Invalid JSON message - 'foreign-keys' / 'columns' item refers to non-existent column: " + fkcol);
                        return;
                    }
                    for (Column col: cols) {
                        if (col.name().equals(fkcol)) {
                            fkColumns.add(col);
                            break;
                        }
                    }
                }

                // check that fk table pk column count and types match fk columns

                Iterator<Column> it = fkTable.pk().columns().iterator();
                Iterator<Column> fkit = fkColumns.iterator();
                while (fkit.hasNext() && it.hasNext()) {
                    Column fkcol = fkit.next();
                    Column pkcol = it.next();
                    if (!fkcol.typeSpec().equals(pkcol.typeSpec())) {
                        responder.invalidRequest("Invalid JSON message - 'foreign-keys' / 'columns' type spec mismatch: " + fkcol.typeSpec() + " vs. " + pkcol.typeSpec());
                        return;
                    }
                }
                if (it.hasNext() || fkit.hasNext()) {
                    responder.invalidRequest("Invalid JSON message - 'foreign-keys' / 'columns' mismatch for: " + o);
                    return;
                }
                fks.add(new ForeignKey(fkColumns, new TableRef(fkTable.schema(), fkTable.name())));
            }
        }

        // compose ddl
        table = new Table(tableRef.schema(), tableRef.name(), cols, pk, fks);
        String ddl = table.ddl(getCatalog());

        // check if schema exists. if it doesn't, check that config allows us to create a new schema
        // also check if the schema we're trying to create isn't deliberately hidden in config
        boolean createNewSchema = false;
        if (!getCatalog().schemas().contains(tableRef.schema())) {
            PgSqlRootConfigResource conf = configuration();
            List<String> exposed = conf.exposedSchemas();
            List<String> blocked = conf.blockedSchemas();
            createNewSchema = (blocked == null || !blocked.contains(tableRef.schema())) &&
                    (exposed == null || exposed.isEmpty() || exposed.contains(tableRef.schema()));

            if (!createNewSchema || !conf.allowCreateSchema()) {
                responder.invalidRequest("Not allowed to create a new schema");
                return;
            }
        }

        // execute ddl
        try (Connection c = getConnection()) {
            // if schema may need to be created now is the time
            if (createNewSchema) {
                try (PreparedStatement ps = c.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + tableRef.quotedSchema())) {
                    ps.execute();
                }
            }
            try (PreparedStatement ps = c.prepareStatement(ddl)) {
                ps.execute();
            }
        }

        // trigger schema reread
        configResource.reloadSchema();

        responder.resourceRead(new PgSqlTableSchemaResource(this, getCatalog().table(tableRef).id()));
    }

    private Column column(TableRef table, ResourceState col) {
        String name = col.getPropertyAsString("name");
        String type = col.getPropertyAsString("type");
        Integer size = col.getPropertyAsInteger("size");
        if (size == null) {
            size = -1;
        }
        Boolean nullable = col.getPropertyAsBoolean("nullable");
        if (nullable == null) {
            nullable = Boolean.TRUE;
        }
        Boolean unique = col.getPropertyAsBoolean("unique");
        if (unique == null) {
            unique = Boolean.FALSE;
        }
        /*
        boolean notNull = false;
        boolean unique = false;

        List modifiers = col.getPropertyAsList("modifiers");
        if (modifiers != null) {
            for (Object val : modifiers) {
                if (val == null) {
                    throw new RuntimeException("Invalid value for modifier: " + val);
                }
                if (val instanceof String == false) {
                    throw new RuntimeException("Invalid value for modifier: " + val);
                }
                switch (((String) val).toLowerCase()) {
                    case "not null":
                        notNull = true;
                        break;
                    case "unique":
                        unique = true;
                        break;
                    default:
                        throw new RuntimeException("Invalid value for modifier: " + val);
                }
            }
        }
        */
        return new Column(table, name, type, size, !nullable, unique);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // this is a way to trigger schema refresh from db
        super.updateProperties(ctx, state, responder);
    }
}