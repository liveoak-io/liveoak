package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.QueryBuilder;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.async.DefaultRootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
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
    public Connection connection() throws SQLException {
        return configResource.connection();
    }

    public Catalog catalog() {
        return configResource.catalog();
    }

    public QueryBuilder queryBuilder() {
        return configResource.queryBuilder();
    }

    public PgSqlCRUDController controller() {
        return configResource.controller();
    }

    public void reloadSchema() throws SQLException {
        configResource.reloadSchema();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        // determine table names
        List<String> tables = catalog().tableIds();

        // store to ctx attributes to pass on to readMembers
        ctx.requestAttributes().setAttribute(TABLE_NAMES, tables);

        List<Resource> links = new LinkedList<>();
        MapResource batch = new MapResource();
        batch.put("rel", "batch");
        batch.put("href", uri() + "/" + BATCH_ENDPOINT);
        links.add(batch);
        sink.accept("links", links);

        // here only set num of tables as size
        sink.accept("count", tables.size());

        // maybe some other things to do with db as a whole
        sink.accept("type", "database");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
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
        List<String> tables = catalog().tableIds();
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

        Table table;
        try {
            table = controller().parseCreateTableRequest(state);
        } catch(RequestProcessingException e) {
            responder.error(e.errorType(), e.getMessage(), e.getCause());
            return;
        }

        // execute ddl
        try (Connection c = connection()) {
            queryBuilder().executeCreateTable(c, table);
        }

        // trigger schema reread
        reloadSchema();

        responder.resourceRead(new PgSqlTableSchemaResource(this, catalog().table(table.tableRef()).id()));
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // this is a way to trigger schema refresh from db
        super.updateProperties(ctx, state, responder);
    }
}