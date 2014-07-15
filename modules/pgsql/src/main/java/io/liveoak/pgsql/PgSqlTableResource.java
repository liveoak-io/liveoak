package io.liveoak.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.QueryBuilder;

import io.liveoak.pgsql.meta.Table;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlTableResource implements Resource {

    private PgSqlRootResource parent;
    private String id;
    private QueryResults results;

    public PgSqlTableResource(PgSqlRootResource root, String table) {
        this.parent = root;
        this.id = table;
    }

    @Override
    public PgSqlRootResource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        // perform select and store it for readMembers
        results = queryTable(id, null, ctx);

        sink.accept("count", results.count());
        sink.accept("type", "collection");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (Row row: results.rows()) {
            sink.accept(new PgSqlRowResource(this, row));
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String childId, Responder responder) throws Exception {
        QueryResults results = queryTable(id, childId, ctx);

        if (results.count() == 0) {
            responder.noSuchResource( id );
        } else {
            responder.resourceRead(new PgSqlRowResource(this, results.rows().get(0)));
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        // insert a new record into a table
        Resource.super.createMember(ctx, state, responder);
        // also handle expanded many-to-one / one-to-many
    }

    QueryResults queryResults() {
        return results;
    }

    private QueryResults queryTable(String table, String id, RequestContext ctx) throws SQLException {
        Catalog cat = parent.getCatalog();
        Table t = cat.tableById(table);
        try (Connection con = parent.getConnection()) {
            QueryBuilder qb = new QueryBuilder(cat);
            if (id == null) {
                return qb.querySelectFromTable(ctx, con, t);
            } else {
                return qb.querySelectFromTableWhereIds(ctx, con, t, new String[] {id});
            }
        }
    }
}
