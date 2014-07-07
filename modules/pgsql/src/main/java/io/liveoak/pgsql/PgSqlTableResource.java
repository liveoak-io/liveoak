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
import io.liveoak.pgsql.meta.QueryBuilder;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlTableResource implements Resource {

    private static final String QUERY_RESULTS = "pg.query.results";

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

        // store to ctx attributes to pass on to readMembers
        //ctx.requestAttributes().setAttribute(QUERY_RESULTS, results);

        // here only set num of tables as size
        sink.accept("count", results.count());

        // maybe some other things to do with db as a whole
        sink.accept("type", "collection");
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        //QueryResults results = (QueryResults) ctx.requestAttributes().getAttribute(QUERY_RESULTS);
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

    QueryResults queryResults() {
        return results;
    }

    private QueryResults queryTable(String table, String id, RequestContext ctx) throws SQLException {
        try (Connection con = parent.getConnection()) {
            QueryBuilder qb = new QueryBuilder(parent.getCatalog());
            if (id == null) {
                return qb.querySelectFromTable(ctx, con, table);
            } else {
                return qb.querySelectFromTableWhereIds(ctx, con, table, new String[] {id});
            }
        }
    }
}
