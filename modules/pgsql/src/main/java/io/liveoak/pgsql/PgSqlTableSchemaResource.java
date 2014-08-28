/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.ForeignKey;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.pgsql.meta.TableRef;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlTableSchemaResource  implements Resource {

    private PgSqlRootResource parent;
    private String id;

    public PgSqlTableSchemaResource(PgSqlRootResource root, String table) {
        this.parent = root;
        this.id = table;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id + ";schema";
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        Table table = parent.catalog().table(new TableRef(id));

        if (table == null) {
            throw new IllegalStateException("No table definition found for: " + id);
        }

        // "columns", "primary-key", "foreign-keys"
        List<MapResource> cols = new ArrayList<>(table.columns().size());
        for (Column c: table.columns()) {
            MapResource col = new MapResource();
            col.put("name", c.name());
            col.put("type", c.type());
            col.put("size", c.size());
            col.put("nullable", !c.notNull());
            col.put("unique", c.unique());
            cols.add(col);
        }

        sink.accept("columns", cols);

        List<Column> pkColumns = table.pk().columns();
        List<String> pkcols = new LinkedList<>();
        for (Column c: pkColumns) {
            pkcols.add(c.name());
        }

        sink.accept("primary-key", pkcols);

        List<ForeignKey> fks = table.foreignKeys();
        if (fks != null && fks.size() > 0) {
            List<MapResource> fkspecs = new LinkedList<>();
            for (ForeignKey fk : fks) {
                MapResource fkspec = new MapResource();
                fkspec.put("table", fk.tableRef().schemaName());

                List<String> fkcols = new LinkedList<>();
                List<Column> fkColumns = fk.columns();
                for (Column c: fkColumns) {
                    fkcols.add(c.name());
                }
                fkspec.put("columns", fkcols);
                fkspecs.add(fkspec);
            }
            sink.accept("foreign-keys", fkspecs);
        }

        sink.accept("ddl", table.ddl());
        sink.close();
    }
}
