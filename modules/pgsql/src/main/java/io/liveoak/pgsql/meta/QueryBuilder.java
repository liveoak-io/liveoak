package io.liveoak.pgsql.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.liveoak.pgsql.data.Id;
import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
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

    public String selectAllFromTable(String table) {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }

        return selectAllFromTable(tableDef);
    }

    public String selectAllFromTable(Table table) {
        return "SELECT * FROM " + table.quotedSchemaName();
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, Table table) throws SQLException {
        return con.prepareStatement(selectAllFromTable(table));
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, String table) throws SQLException {
        return con.prepareStatement(selectAllFromTable(table));
    }

    public PreparedStatement prepareSelectFromTableWhereId(Connection con, String table, String id) throws SQLException {
        return prepareSelectFromTableWhereIds(con, table, new String[]{id});
    }

    public PreparedStatement prepareSelectFromTableWhereIds(Connection con, String table, String[] ids) throws SQLException {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }

        return prepareSelectFromTableWhereIds(con, tableDef, ids);
    }

    public PreparedStatement prepareSelectFromTableWhereIds(Connection con, Table table, String[] ids) throws SQLException {

        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("ids is null or empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(selectAllFromTable(table) + " WHERE ");

        LinkedList<String[]> parsed = new LinkedList<>();
        List<Column> cols = null;
        int i = 0;
        for (String id: ids) {
            if (i > 0) {
                sb.append(" OR ");
            }
            String[] vals = PrimaryKey.splitId(id);
            parsed.add(vals);

            cols = table.pk().columns();

            if (cols.size() != vals.length) {
                throw new IllegalStateException("Id is incompatible with table definition: " + table + " contains "
                        + cols.size() + " columns, while id " + id + " contains " + vals.length + " components");
            }
            int j = 0;
            for (Column c : cols) {
                if (j > 0) {
                    sb.append(" AND ");
                } else if (ids.length > 1) {
                    sb.append(" (");
                }
                sb.append(c.quotedName()).append("=?");
                j++;
            }
            if (ids.length > 1) {
                sb.append(")");
            }
        }

        PreparedStatement ps = con.prepareStatement(sb.toString());

        i = 1;
        for (String[] vals: parsed) {
            for (String v: vals) {
                cols.get(i % cols.size()).bindValue(ps, i, v);
                i++;
            }
        }

        return ps;
    }


    public PreparedStatement prepareSelectFromTableWhere(Connection con, Table table, List<Column> columns, List<Object> values) throws SQLException {
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("values is null or empty");
        }

        if (columns.size() != values.size()) {
            throw new IllegalStateException("Values size doesn't match columns size: (columns: " + columns + ", values: " + values + ")");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(selectAllFromTable(table) + " WHERE ");

        //List<Column> cols = null;
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
                if (resRef instanceof ResourceState) {
                    Id fkId = new Id(catalog.table(fk.tableRef()).pk(), ((ResourceState) resRef).id());
                    values.add(fkId.valueForIndex(fk.indexForColumn(c.name())));
                } else {
                    throw new RuntimeException("Invalid value for field: " + field);
                }
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

    private PreparedStatement prepareDelete(Connection con, Table table, String id) throws SQLException {

        Id tableId = new Id(table.pk(), id);
        StringBuilder sb = new StringBuilder("DELETE FROM " + table.quotedSchemaName() + " WHERE ");

        List<Object> values = new LinkedList<>();
        List<Column> columns = new LinkedList<>();

        int i = 0;
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

    private PreparedStatement prepareDeleteTable(Connection con, Table t) throws SQLException {
        StringBuilder sb = new StringBuilder("DROP TABLE " + t.quotedSchemaName());
        return con.prepareStatement(sb.toString());
    }

    private String selfHrefId(String field, Object val) {
        if (val == null || val instanceof ResourceState == false) {
            throw new RuntimeException("Invalid value for '" + field + "': " + val);
        }
        /*
        Object self = ((ResourceState) val).getProperty("self");
        if (self == null || self instanceof ResourceState == false) {
            throw new RuntimeException("Not a valid resource reference - no 'self' - for '" + field + "': " + val);
        }

        String href = ((ResourceState) val).getPropertyAsString("href");
        if (href == null) {
            throw new RuntimeException("Not a valid resource reference - no 'self/href' - for '" + field + "': " + val);
        }
        return new ResourcePath(href).tail().toString();
        */
        return new ResourcePath(((ResourceState) val).uri().toString()).tail().toString();
    }

    public QueryResults querySelectFromTableWhereIds(RequestContext ctx, Connection con, Table table, String [] ids) throws SQLException {
        if (ids != null && ids.length > 0) {
            return query(ctx, prepareSelectFromTableWhereIds(con, table, ids));
        } else {
            return query(ctx, prepareSelectAllFromTable(con, table));
        }
    }

    public QueryResults querySelectFromTableWhereIds(RequestContext ctx, Connection con, String table, String [] ids) throws SQLException {
        if (ids != null && ids.length > 0) {
            return query(ctx, prepareSelectFromTableWhereIds(con, table, ids));
        } else {
            return query(ctx, prepareSelectAllFromTable(con, table));
        }
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, Table table) throws SQLException {
        return query(ctx, prepareSelectAllFromTable(con, table));
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, String table) throws SQLException {
        return query(ctx, prepareSelectAllFromTable(con, table));
    }

    public QueryResults querySelectFromTable(RequestContext ctx, Connection con, Table table, List<Column> columns, List<Object> values) throws SQLException {
        return query(ctx, prepareSelectFromTableWhere(con, table, columns, values));
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

    public void executeDelete(RequestContext ctx, Connection con, Table table, String id) throws SQLException {
        try (PreparedStatement ps = prepareDelete(con, table, id)) {
            ps.executeUpdate();
        }
    }

    public void executeDeleteTable(Connection con, Table t) throws SQLException {
        try (PreparedStatement ps = prepareDeleteTable(con, t)) {
            ps.executeUpdate();
        }
    }
}
