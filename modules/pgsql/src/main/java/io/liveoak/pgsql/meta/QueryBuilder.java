package io.liveoak.pgsql.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.data.Id;
import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceRef;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class QueryBuilder {

    private static final Logger log = Logger.getLogger(QueryBuilder.class);

    private Catalog catalog;

    public QueryBuilder(Catalog catalog) {
        this.catalog = catalog;
    }
/*
    public String selectAllFromTable(String table) {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }

        return selectAllFromTable(tableDef);
    }
*/
    public String selectAllFromTable(Table table) {
        return "SELECT * FROM " + table.quotedSchemaName();
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, Pagination pagination, String table) throws SQLException {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }
        return prepareSelectAllFromTable(con, pagination, tableDef);
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, Pagination pagination, Table table) throws SQLException {
        String offset = pagination.offset() > 0 ? " OFFSET " + pagination.offset() : "";
        return con.prepareStatement(selectAllFromTable(table) + " LIMIT " + pagination.limit() + offset);
    }

    public PreparedStatement prepareSelectFromTableWhereId(Connection con, Pagination pagination, String table, String id) throws SQLException {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }

        return prepareSelectFromTableWhereId(con, pagination, tableDef, id);
    }

    public PreparedStatement prepareSelectFromTableWhereId(Connection con, Pagination pagination, Table table, String id) throws SQLException {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id is null or empty");
        }

        return prepareSelectFromTableWhere(con, pagination, table, table.pk().columns(), PrimaryKey.splitIdAsList(id));
    }

    public PreparedStatement prepareSelectFromTableWhere(Connection con, Pagination pagination, Table table, List<Column> columns, List<?> values) throws SQLException {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("values is null or empty");
        }

        if (columns.size() != values.size()) {
            throw new IllegalStateException("Values size doesn't match columns size: (columns: " + columns + ", values: " + values + ")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(selectAllFromTable(table) + " WHERE ");

        int i = 0;
        for (Column col: columns) {
            if (i > 0) {
                sb.append(" AND ");
            }

            sb.append(col.quotedName()).append("=?");
        }

        String offset = pagination.offset() > 0 ? " OFFSET " + pagination.offset() : "";
        sb.append(" LIMIT " + pagination.limit() + offset);

        PreparedStatement ps = con.prepareStatement(sb.toString());

        i = 0;
        for (Object val: values) {
            columns.get(i).bindValue(ps, i + 1, val);
            i++;
        }

        return ps;
    }

    public PreparedStatement prepareInsert(Connection con, Table table, ResourceState state) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + table.quotedSchemaName() + " (");
        int i = 0;
        for (Column c: table.columns()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(c.quotedName());
            i++;
        }
        sb.append(") VALUES (");

        for (i = 0; i < table.columns().size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");

        // For PK columns we have to use state.id(), parse it into column values and then set
        // If PK column values are specified via properties as well, then they need to be equal to id
        // If not, that can either be marked as an error, or they can be simply ignored
        // Or they can be used if id() is null
        List<Object> pkvals = new ArrayList<>();
        String id = state.id();
        if (id != null) {
            pkvals.addAll(PrimaryKey.splitIdAsList(id));
        } else {
            for (Column c: table.pk().columns()) {
                Object val = state.getProperty(c.name());
                if (val == null) {
                    throw new RuntimeException("Neither id, nor primary key column is set: " + c.name());
                }
                pkvals.add(val);
            }
        }

        PreparedStatement ps = con.prepareStatement(sb.toString());
        i = 1;
        PrimaryKey pk = table.pk();
        for (Column c: table.columns()) {
            Object val = null;

            Key key = table.keyForColumnName(c.name());

            if (key instanceof ForeignKey) {
                // if it's a FK then extract referenced id
                String fkField = catalog.table(((ForeignKey) key).tableRef()).id();
                val = selfHrefId(fkField, state.getProperty(fkField));

            } else if (key instanceof PrimaryKey) {
                // if it's a PK column use appropriate value
                int idx = 0;
                for (Column pkc : pk.columns()) {
                    if (pkc.name().equals(c.name())) {
                        val = pkvals.get(idx);
                        break;
                    }
                    idx++;
                }
            } else {
                // any other column - non-pk, non-fk column
                val = state.getProperty(c.name());
            }

            // bindValue will convert from String to appropriate value if needed
            c.bindValue(ps, i, val);
            i++;
        }

        return ps;
    }

    public PreparedStatement prepareUpdate(Connection con, Table table, ResourceState state) throws SQLException {
        StringBuilder sb = new StringBuilder("UPDATE " + table.quotedSchemaName() + " SET ");

        int i = 0;
        List<Object> values = new LinkedList<>();
        List<Column> columns = new LinkedList<>();

        for (Column c: table.columns()) {
            if (table.pkForColumnName(c.name()) != null) {
                // skip if it's a PK
                continue;
            }
            if (i > 0) {
                sb.append(",");
            }
            sb.append(c.quotedName()).append("=?");

            ForeignKey fk = table.foreignKeyForColumnName(c.name());
            if (fk != null) {
                String field = catalog.table(fk.tableRef()).id();
                Object resRef = state.getProperty(field);
                String id = null;
                if (resRef instanceof ResourceRef) {
                    id = ((ResourceRef) resRef).resourcePath().tail().toString();
                } else if (resRef instanceof ResourceState) {
                    id = ((ResourceState) resRef).id();
                }
                if (id == null) {
                    throw new RuntimeException("Invalid value for field: " + field);
                }
                Id fkId = new Id(catalog.table(fk.tableRef()).pk(), id);
                values.add(fkId.valueForIndex(fk.indexForColumn(c.name())));
            } else {
                values.add(state.getProperty(c.name()));
            }
            columns.add(c);
            i++;
        }
        sb.append(" WHERE ");

        i = 0;
        Id tableId = new Id(table.pk(), state.id());
        for (Column c: table.pk().columns()) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append(c.quotedName()).append("=?");
            values.add(tableId.valueForIndex(i));
            columns.add(c);
            i++;
        }

        PreparedStatement ps = con.prepareStatement(sb.toString());

        Iterator valIt = values.iterator();
        Iterator<Column> colIt = columns.iterator();
        i = 1;
        while(colIt.hasNext() && valIt.hasNext()) {
            colIt.next().bindValue(ps, i, valIt.next());
            i++;
        }

        return ps;
    }

    /**
     *  ForeignKey identifies the table, and id fields to use for the delete
     */
    private PreparedStatement prepareDeleteWhere(Connection con, Table table, List<Column> columns, List<Object> values) throws SQLException {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("values is null or empty");
        }

        if (columns.size() != values.size()) {
            throw new IllegalStateException("Values size doesn't match columns size: (columns: " + columns + ", values: " + values + ")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM " + table.quotedSchemaName() + " WHERE ");

        int i = 0;
        for (Column col: columns) {
            if (i > 0) {
                sb.append(" AND ");
            }

            sb.append(col.quotedName()).append("=?");
        }

        PreparedStatement ps = con.prepareStatement(sb.toString());

        i = 0;
        for (Object val: values) {
            columns.get(i).bindValue(ps, i + 1, val);
            i++;
        }

        return ps;
    }

    private PreparedStatement prepareDeleteTable(Connection con, Table t) throws SQLException {
        StringBuilder sb = new StringBuilder("DROP TABLE " + t.quotedSchemaName());
        return con.prepareStatement(sb.toString());
    }

    private String selfHrefId(String field, Object val) {
        if (val == null || val instanceof ResourceState == false) {
            throw new RuntimeException("Invalid value for '" + field + "': " + val);
        }

        String href = null;
        if (val instanceof ResourceRef) {
            href = ((ResourceRef) val).uri().toString();
            if (href == null) {
                throw new RuntimeException("Not a valid resource reference - empty uri - for '" + field + "': " + val);
            }
        } else {
            Object self = ((ResourceState) val).getProperty("self");
            if (self == null || self instanceof ResourceState == false) {
                throw new RuntimeException("Not a valid resource reference - no 'self' - for '" + field + "': " + val);
            }

            href = ((ResourceState) self).getPropertyAsString("href");
            if (href == null) {
                throw new RuntimeException("Not a valid resource reference - no 'self/href' - for '" + field + "': " + val);
            }
        }
        return new ResourcePath(href).tail().toString();
    }

    public QueryResults querySelectFromTableWhereId(RequestContext ctx, Connection con, Table table, String id) throws SQLException {
        if (id != null && id.length() > 0) {
            return query(ctx, prepareSelectFromTableWhereId(con, ctx.pagination(), table, id));
        } else {
            return query(ctx, prepareSelectAllFromTable(con, ctx.pagination(), table));
        }
    }

    public QueryResults querySelectFromTableWhereId(RequestContext ctx, Connection con, String table, String id) throws SQLException {
        if (id != null && id.length() > 0) {
            return query(ctx, prepareSelectFromTableWhereId(con, ctx.pagination(), table, id));
        } else {
            return query(ctx, prepareSelectAllFromTable(con, ctx.pagination(), table));
        }
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, Table table) throws SQLException {
        return query(ctx, prepareSelectAllFromTable(con, ctx.pagination(), table));
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, String table) throws SQLException {
        return query(ctx, prepareSelectAllFromTable(con, ctx.pagination(), table));
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, Table table, List<Column> columns, List<Object> values) throws SQLException {
        return query(ctx, prepareSelectFromTableWhere(con, ctx.pagination(), table, columns, values));
    }

    public QueryResults query(RequestContext ctx, PreparedStatement ps) throws SQLException {
        try (PreparedStatement s = ps) {
            s.setMaxRows(ctx.pagination().limit());
            try (ResultSet rs = s.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();

                ArrayList<String> columnNames = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    columnNames.add(meta.getColumnName(i + 1));
                }

                LinkedList<Row> rows = new LinkedList<>();
                while (rs.next()) {
                    ArrayList<Object> row = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        row.add(rs.getObject(i + 1));
                    }
                    rows.add(new Row(columnNames, row));
                }

                return new QueryResults(columnNames, rows);
            } catch (Exception e) {
                log.error("Exception while executing a query: " + ps, e);
            }
        }
        return new QueryResults();
    }

    public String executeInsert(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {
        String id = extractId(table, state);
        try (PreparedStatement ps = prepareInsert(con, table, state)) {
            ps.execute();
        }
        return id;
    }

    private String extractId(Table table, ResourceState state) {
        String id = state.id();
        if (id != null) {
            return id;
        } else {
            List<Object> pkvals = new ArrayList<>();
            for (Column c: table.pk().columns()) {
                Object val = state.getProperty(c.name());
                if (val == null) {
                    throw new RuntimeException("Neither id, nor primary key column is set: " + c.name());
                }
                pkvals.add(val);
            }
            return PrimaryKey.spliceId(pkvals);
        }
    }

    public void executeUpdate(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {
        try (PreparedStatement ps = prepareUpdate(con, table, state)) {
            ps.executeUpdate();
        }
    }

    public void executeDelete(RequestContext ctx, Connection con, Table table, String id, boolean cascade) throws SQLException {

        Id tableId = new Id(table.pk(), id);

        List<Object> values = new LinkedList<>();
        List<Column> columns = new LinkedList<>();

        int i = 0;
        for (Column c: table.pk().columns()) {
            values.add(tableId.valueForIndex(i));
            columns.add(c);
            i++;
        }

        if (cascade) {
            // first take care of references
            for(ForeignKey ref: table.referredKeys()) {
                TableRef refTableRef = ref.columns().get(0).tableRef();
                Table refTable = catalog.table(refTableRef);
                try (PreparedStatement ps = prepareDeleteWhere(con, refTable, ref.columns(), values)) {
                    ps.executeUpdate();
                }
            }
        }
        try (PreparedStatement ps = prepareDeleteWhere(con, table, columns, values)) {
            ps.executeUpdate();
        }
    }

    public void executeDeleteTable(Connection con, Table t) throws SQLException {
        try (PreparedStatement ps = prepareDeleteTable(con, t)) {
            ps.executeUpdate();
        }
    }
}
