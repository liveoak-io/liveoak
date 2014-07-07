package io.liveoak.pgsql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.pgsql.data.Row;
import io.liveoak.pgsql.meta.Catalog;
import io.liveoak.pgsql.meta.Column;
import io.liveoak.pgsql.meta.ForeignKey;
import io.liveoak.pgsql.meta.PrimaryKey;
import io.liveoak.pgsql.meta.Table;
import io.liveoak.pgsql.meta.TableRef;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlRowResource implements Resource {

    private PgSqlTableResource parent;
    private String id;
    private Row row;

    public PgSqlRowResource(PgSqlTableResource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public PgSqlRowResource(PgSqlTableResource parent, Row row) {
        this.parent = parent;
        this.row = row;
        Table table = parent.parent().getCatalog().table(new TableRef(parent.id()));
        List<Column> pk = table.pk().columns();
        StringBuilder sb = new StringBuilder();
        for (Column c: pk) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(row.value(c.name()));
        }
        id = sb.toString();
    }

    public void row(Row row) {
        this.row = row;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        Catalog cat = parent.parent().getCatalog();
        Table table = cat.table(new TableRef(parent.id()));
        HashMap<ForeignKey, String[]> fkMap = new HashMap<>();

        for (Map.Entry<String, Object> ent: row.asMap().entrySet()) {
            // encode FKs as ResourceRefs
            // use container? to expand ResourceRefs - to optimize we need to expand many ResourceRefs in one call
            // That requires a direct api - e.g. readMembers(List<String> ids)
            // Or ids query param that is a generalized queryById
            // or a specific query param or url syntax e.g. /test/orders/0001|0002|0003|0004

            // Naming convention - we use table name as a key for ResourceRef rather than a FK column name
            // since there may be multiple columns involved in construction of the reference

            // We withold FK columns until we make a full pass, and then process them at the end where we have all the
            // FK column values ready, and can construct identifiers
            ForeignKey fk = table.foreignKeyForColumnName(ent.getKey());
            if (fk != null) {
                String[] cols = fkMap.get(fk);
                if (cols == null) {
                    cols = new String[fk.columns().size()];
                    fkMap.put(fk, cols);
                }
                int i = 0;
                for (Column col: fk.columns()) {
                    if (col.name().equals(ent.getKey())) {
                        cols[i] = String.valueOf(ent.getValue()); // TODO: helper method asString
                    }
                    i++;
                }
            } else {
                sink.accept(ent.getKey(), ent.getValue());
            }
        }

        // if there are any fks it's time to process them
        for (Map.Entry<ForeignKey, String[]> ent: fkMap.entrySet()) {
            String fkTable = ent.getKey().tableRef().name();
            sink.accept(fkTable, new PgSqlResourceRef(
                    new PgSqlTableResource(parent.parent(), fkTable),
                    PrimaryKey.spliceId(ent.getValue())));
        }

        sink.close();
    }
}
