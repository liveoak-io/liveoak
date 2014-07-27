package io.liveoak.pgsql;

import java.sql.Connection;

import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.pgsql.meta.QueryBuilder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlResourceRef extends PgSqlRowResource {

    public PgSqlResourceRef(PgSqlTableResource parent, String id) {
        super(parent, id);
    }

    public PgSqlTableResource parent() {
        return (PgSqlTableResource) super.parent();
    }

    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        QueryResults results;
        try (Connection con = parent().parent().getConnection()) {
            QueryBuilder qb = new QueryBuilder(parent().parent().getCatalog());
            results = qb.querySelectFromTableWhereId(con, parent().id(), id());
        }

        for (Row row: results.rows()) {
            row(row);
            break;
        }

        super.readProperties(ctx, sink);
    }
}
