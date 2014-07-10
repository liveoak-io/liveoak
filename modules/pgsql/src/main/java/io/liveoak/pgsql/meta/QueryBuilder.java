package io.liveoak.pgsql.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.spi.RequestContext;
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
}
