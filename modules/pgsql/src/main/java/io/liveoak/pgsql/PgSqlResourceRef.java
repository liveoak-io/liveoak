/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.sql.Connection;

import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
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
        try (Connection con = parent().parent().connection()) {
            results = queryBuilder().querySelectFromTableWhereId(con, parent().id(), id());
        }

        for (Row row: results.rows()) {
            row(row);
            break;
        }

        super.readProperties(ctx, sink);
    }
}
