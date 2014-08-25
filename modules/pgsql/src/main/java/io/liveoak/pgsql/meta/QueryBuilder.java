package io.liveoak.pgsql.meta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.codec.DefaultResourceRef;
import io.liveoak.pgsql.PgSqlBatchItem;
import io.liveoak.pgsql.data.Pair;
import io.liveoak.pgsql.data.Id;
import io.liveoak.pgsql.data.QueryResults;
import io.liveoak.pgsql.data.Row;
import io.liveoak.pgsql.sql.And;
import io.liveoak.pgsql.sql.Expression;
import io.liveoak.pgsql.sql.ExpressionWalker;
import io.liveoak.pgsql.sql.GreaterThan;
import io.liveoak.pgsql.sql.GreaterThanOrEqual;
import io.liveoak.pgsql.sql.Identifier;
import io.liveoak.pgsql.sql.LessThan;
import io.liveoak.pgsql.sql.LessThanOrEqual;
import io.liveoak.pgsql.sql.LogicalOperator;
import io.liveoak.pgsql.sql.Not;
import io.liveoak.pgsql.sql.NotEqual;
import io.liveoak.pgsql.sql.Operator;
import io.liveoak.pgsql.sql.Or;
import io.liveoak.pgsql.sql.RelationalOperand;
import io.liveoak.pgsql.sql.RelationalOperator;
import io.liveoak.pgsql.sql.Value;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.Sorting;
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

    public String selectAllFromTable(Table table) {
        return "SELECT * FROM " + table.quotedSchemaName();
    }

    public String selectFromTable(Table table, List<Column> columns) {
        if (columns == null) {
            throw new IllegalArgumentException("columns == null");
        }

        StringBuilder sb = new StringBuilder("SELECT ");
        int i = 0;
        for (Column c: columns) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(c.quotedName());
            i++;
        }
        sb.append(" FROM " + table.quotedSchemaName());

        return sb.toString();
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, String table, Sorting sorting, Pagination pagination) throws SQLException {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }
        return prepareSelectAllFromTable(con, tableDef, sorting, pagination);
    }

    public PreparedStatement prepareSelectAllFromTable(Connection con, Table table, Sorting sorting, Pagination pagination) throws SQLException {
        StringBuilder sb = new StringBuilder(selectAllFromTable(table));

        if (sorting != null) {
            StringBuilder orderBy = new StringBuilder();
            int i = 0;
            for (Sorting.Spec spec: sorting) {
                if (i > 0) {
                    orderBy.append(",");
                }
                orderBy.append(spec.name());
                if (!spec.ascending()) {
                    orderBy.append(" DESC");
                }
                i++;
            }
            if (orderBy.length() > 0) {
                sb.append(" ORDER BY ").append(orderBy);
            }
        }
        sb.append(" LIMIT " + pagination.limit());

        if (pagination.offset() > 0) {
            sb.append(" OFFSET " + pagination.offset());
        }
        return con.prepareStatement(sb.toString());
    }

    public PreparedStatement prepareSelectFromTableWhereId(Connection con, String table, String id, Pagination pagination) throws SQLException {
        Table tableDef = catalog.table(new TableRef(table));
        if (tableDef == null) {
            throw new IllegalStateException("No such table: " + table);
        }

        return prepareSelectFromTableWhereId(con, tableDef, id, pagination);
    }

    public PreparedStatement prepareSelectFromTableWhereId(Connection con, Table table, String id, Pagination pagination) throws SQLException {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id is null or empty");
        }

        return prepareSelectFromTableWhere(con, table, null, table.pk().columns(), PrimaryKey.splitIdAsList(id), (Sorting) null, pagination);
    }

    public PreparedStatement prepareSelectFromTableWhere(Connection con, Table table, List<Column> resultColumns, List<Column> whereColumns, List<?> whereValues, Sorting sorting, Pagination pagination) throws SQLException {
        if (whereValues == null || whereValues.size() == 0) {
            throw new IllegalArgumentException("values is null or empty");
        }

        if (whereColumns.size() != whereValues.size()) {
            throw new IllegalStateException("Values size doesn't match columns size: (columns: " + whereColumns + ", values: " + whereValues + ")");
        }

        StringBuilder sb = new StringBuilder();
        if (resultColumns == null) {
            sb.append(selectAllFromTable(table));
        } else {
            sb.append(selectFromTable(table, resultColumns));
        }
        sb.append(" WHERE ");

        int i = 0;
        for (Column col: whereColumns) {
            if (i > 0) {
                sb.append(" AND ");
            }

            sb.append(col.quotedName()).append("=?");
        }

        if (sorting != null) {
            sb.append(" ORDER BY ");
            int j = 0;
            for (Sorting.Spec spec: sorting) {
                if (j > 0) {
                    sb.append(",");
                }
                sb.append(spec.name());
                if (!spec.ascending()) {
                    sb.append(" DESC");
                }
                j++;
            }
        }

        if (pagination != null) {
            String offset = pagination.offset() > 0 ? " OFFSET " + pagination.offset() : "";
            sb.append(" LIMIT " + pagination.limit() + offset);
        }

        PreparedStatement ps = con.prepareStatement(sb.toString());

        i = 0;
        for (Object val: whereValues) {
            whereColumns.get(i).bindValue(ps, i + 1, val);
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
                String fkField = ((ForeignKey) key).fieldName();
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
                String field = fk.fieldName();
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

    public QueryResults querySelectFromTableWhereId(Connection con, Table table, String id) throws SQLException {
        if (id != null && id.length() > 0) {
            return query(prepareSelectFromTableWhereId(con, table, id, (Pagination) null), (Pagination) null);
        } else {
            return query(prepareSelectAllFromTable(con, table, (Sorting) null, Pagination.NONE), Pagination.NONE);
        }
    }

    public QueryResults querySelectFromTableWhereId(Connection con, String table, String id) throws SQLException {
        if (id != null && id.length() > 0) {
            return query(prepareSelectFromTableWhereId(con, table, id, (Pagination) null), (Pagination) null);
        } else {
            return query(prepareSelectAllFromTable(con, table, (Sorting) null, Pagination.NONE), Pagination.NONE);
        }
    }

    public QueryResults querySelectFromTable(Connection con, Table table, Sorting sorting, Pagination pagination) throws SQLException {
        return query(prepareSelectAllFromTable(con, table, sorting, pagination), pagination);
    }

    public QueryResults querySelectFromTable(Connection con, String table, Sorting sorting, Pagination pagination) throws SQLException {
        return query(prepareSelectAllFromTable(con, table, sorting, pagination), pagination);
    }

    public QueryResults querySelectFromTableWhere(Connection con, Table table, List<Column> whereColumns, List<Object> whereValues, Sorting sorting, Pagination pagination) throws SQLException {
        return query(prepareSelectFromTableWhere(con, table, null, whereColumns, whereValues, sorting, pagination), pagination);
    }

    public QueryResults querySelectFromTableWhere(Connection con, Table table, List<Column> resultColumns, List<Column> whereColumns, List<Object> whereValues, Sorting sorting, Pagination pagination) throws SQLException {
        return query(prepareSelectFromTableWhere(con, table, resultColumns, whereColumns, whereValues, sorting, pagination), pagination);
    }

    public QueryResults query(PreparedStatement ps, Pagination pagination) throws SQLException {
        try (PreparedStatement s = ps) {
            if (log.isTraceEnabled()) {
                log.trace("sql: " + rawSQL(ps));
            }

            if (pagination != null) {
                s.setMaxRows(pagination.limit());
            }
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

    /*
    public String executeInsert(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {
        String id = extractId(table, state);
        try (PreparedStatement ps = prepareInsert(con, table, state)) {
            ps.execute();
        }
        return id;
    }
    */
    public String executeCreate(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {

        // perform this first, as it serves as an id presence check
        String id = extractId(table, state);

        // first create new record in master table
        // if item exists already this will fail
        try (PreparedStatement ps = prepareInsert(con, table, state)) {
            ps.execute();
        }

        // now take care of references
        for(ForeignKey ref: table.referredKeys()) {
            TableRef refTableRef = ref.columns().get(0).tableRef();
            Table refTable = catalog.table(refTableRef);

            // create children / there can't be any existing yet
            List<Object> listOfNew = state.getPropertyAsList(refTable.id());
            if (listOfNew != null) {
                for (Object item : listOfNew) {
                    ResourceState newState = ((ResourceState) item);

                    // if id is not set on item and foreign key columns on item are the same as its primary key columns,
                    // then set the id - since its value must be equal to master's id
                    if (newState.id() == null && refTable.pk().sameColumnsAs(ref)) {
                        newState.id(id);
                    }

                    // link child back to master - overwriting any existing value
                    newState.putProperty(ref.fieldName(), new DefaultResourceRef(state.uri()));
                    executeCreate(ctx, con, refTable, newState);
                }
            }
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

    public void executeMerge(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {
        executeUpdate(ctx, con, table, state, null, true);
    }

    public void executeUpdate(RequestContext ctx, Connection con, Table table, ResourceState state) throws SQLException {
        executeUpdate(ctx, con, table, state, null, false);
    }

    public void executeUpdate(RequestContext ctx, Connection con, Table table, ResourceState state, ForeignKey master, boolean upsert) throws SQLException {

        // perform this first as it also serves as id check
        Id tableId = new Id(table.pk(), state.id());

        // handle many-to-one relationships
        // if what is in state is not only a reference, but a full nested instance
        // perform upsert on that instance
        for (ForeignKey ref: table.foreignKeys()) {
            ResourceState item = state.getPropertyAsResourceState(ref.fieldName());
            for (String name: item.getPropertyNames()) {
                if (!"self".equals(name)) {
                    executeUpdate(ctx, con, catalog.table(ref.tableRef()), item, ref, true);
                    break;
                }
            }
        }

        // update the record in master table
        // if record with specified id doesn't exist, the update will fail, after which we try insert
        // that should be better performance-wise than checking if it exist or not
        int updateCount;
        try (PreparedStatement ps = prepareUpdate(con, table, state)) {
            updateCount = ps.executeUpdate();
        }
        if (updateCount == 0 && upsert) {
            try (PreparedStatement ps = prepareInsert(con, table, state)) {
                if (log.isTraceEnabled()) {
                    log.trace("sql: " + rawSQL(ps));
                }
                ps.execute();
            }
        }
        List<Object> values = new LinkedList<>();
        List<Column> columns = new LinkedList<>();

        int i = 0;
        for (Column c: table.pk().columns()) {
            values.add(tableId.valueForIndex(i));
            columns.add(c);
            i++;
        }

        // take care of references, but skip master if specified
        for (ForeignKey ref: table.referredKeys()) {
            if (ref.equals(master)) {
                continue;
            }

            TableRef refTableRef = ref.columns().get(0).tableRef();
            Table refTable = catalog.table(refTableRef);

            // if yes, then load ids of referring refTable items first
            QueryResults results = querySelectFromTableWhere(con, refTable, refTable.pk().columns(), ref.columns(), values, null, null);
            String prefix = new ResourcePath(state.uri().toString()).parent().parent().toString() + "/" + refTable.id() + "/";
            List<String> currentUris = prefixElements(extractPksFromResults(results, refTable), prefix);

            List<Object> listOfNew = state.getPropertyAsList(refTable.id());
            List<String> newUris = new LinkedList<>();
            for (Object item: listOfNew) {
                if (item instanceof ResourceState == false) {
                    throw new RuntimeException("Invalid JSON message - unexpected content of " + refTable.id() + " - item should be a JSON object: " + item);
                }
                ResourceState itemState = (ResourceState) item;
                newUris.add(itemState.uri().toString());
            }

            List<ResourceState> updated = new LinkedList<>();

            // compare newUris with currentUris
            List<String> addedUris = new ArrayList(newUris);
            addedUris.removeAll(currentUris);

            // first create newly added
            for (Object item: listOfNew) {
                ResourceState newState = ((ResourceState) item);

                // if id is not set on item and foreign key columns on item are the same as its primary key columns,
                // then set the id - since its value must be equal to master's id
                if (newState.id() == null && refTable.pk().sameColumnsAs(ref)) {
                    newState.id(state.id());
                }

                // link child back to its master - overwriting any existing value
                newState.putProperty(ref.fieldName(), new DefaultResourceRef(state.uri()));

                if (addedUris.contains(newState.uri().toString())) {
                    executeCreate(ctx, con, refTable, newState);
                } else {
                    updated.add(newState);
                }
            }

            // now remove those that are to be removed
            List<String> removedUris = new ArrayList(currentUris);
            removedUris.removeAll(newUris);

            for (String uri: removedUris) {
                executeDelete(ctx, con, refTable, uri.substring(prefix.length()), true);
            }

            // update existing
            for (ResourceState item: updated) {
                // only update if there are some properties present other than just self
                for (String name: item.getPropertyNames()) {
                    if (!"self".equals(name)) {
                        executeUpdate(ctx, con, refTable, item);
                        break;
                    }
                }
            }
        }
    }

    private List<String> prefixElements(List<String> els, String prefix) {
        List<String> prefixed = new ArrayList<>(els.size());
        for (String el: els) {
            prefixed.add(prefix + el);
        }
        return prefixed;
    }

    private List<String> extractPksFromResults(QueryResults results, Table table) {
        List<String> ids = new LinkedList<>();
        for (Row row: results.rows()) {
            List<Object> cols = new LinkedList<>();
            for (Column c: table.pk().columns()) {
                cols.add(row.value(c.name()));
            }
            ids.add(PrimaryKey.spliceId(cols));
        }
        return ids;
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

                // check if refTable has any referredKeys
                if (refTable.referredKeys().size() > 0) {
                    // if yes, then load ids of referring refTable items first
                    QueryResults results = querySelectFromTableWhere(con, refTable, refTable.pk().columns(), ref.columns(), values, null, null);
                    for (Row row: results.rows()) {
                        // and invoke executeDelete for each of them
                        executeDelete(ctx, con, refTable, refTable.pk().idFromRow(row), cascade);
                    }
                } else {
                    try (PreparedStatement ps = prepareDeleteWhere(con, refTable, ref.columns(), values)) {
                        if (log.isTraceEnabled()) {
                            log.trace("sql: " + rawSQL(ps));
                        }
                        ps.executeUpdate();
                    }
                }
            }
        }
        try (PreparedStatement ps = prepareDeleteWhere(con, table, columns, values)) {
            if (log.isTraceEnabled()) {
                log.trace("sql: " + rawSQL(ps));
            }
            ps.executeUpdate();
        }
    }

    public void executeDeleteTable(Connection con, Table t) throws SQLException {
        try (PreparedStatement ps = prepareDeleteTable(con, t)) {
            if (log.isTraceEnabled()) {
                log.trace("sql: " + rawSQL(ps));
            }
            ps.executeUpdate();
        }
    }

    public List<PgSqlBatchItem<Table,?>> executeDeleteTables(Connection c, List<PgSqlBatchItem<Table, ?>> deleteList) throws SQLException {

        List<PgSqlBatchItem<Table, ?>> statuses = new LinkedList<>();

        List<Table> listOfTables = PgSqlBatchItem.asListOfInputs(deleteList);
        Map<Table, PgSqlBatchItem> itemMap = PgSqlBatchItem.asMapOfInputs(deleteList);

        // check that all the referrers are also in deleteList
        // if not - don't even start deleting
        for (Table t: listOfTables) {
            for (ForeignKey fk: t.referredKeys()) {
                Table dep = catalog.table(fk.columns().get(0).tableRef());
                if (! itemMap.containsKey(dep)) {
                    itemMap.get(t).error(new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Table " + dep.id() + " has dependency on " + t.id() + " and should be included for deletion as well"));
                }
            }
        }

        // first find all dependencies, and order them properly
        Set<Table> sorted = catalog.orderByReferred(listOfTables);

        for (Table t: sorted) {
            PgSqlBatchItem<Table, ?> item = itemMap.get(t);
            statuses.add(item);

            if (item.error() != null) {
                continue;
            }
            try {
                executeDeleteTable(c, t);
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("Exception deleting a table: ", e);
                }
                item.error(new ResourceProcessingException(
                        ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, e.getMessage(), e));
            }
        }

        return statuses;
    }

    public void executeCreateTable(Connection con, Table table) throws SQLException {

        // if schema may need to be created now is the time
        boolean newSchemaNeeded = !catalog.schemas().contains(table.tableRef().schema());
        if (newSchemaNeeded) {
            try (PreparedStatement ps = con.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + table.tableRef().quotedSchema())) {
                if (log.isTraceEnabled()) {
                    log.trace("sql: " + rawSQL(ps));
                }
                ps.execute();
            }
        }
        try (PreparedStatement ps = con.prepareStatement(table.ddl())) {
            if (log.isTraceEnabled()) {
                log.trace("sql: " + rawSQL(ps));
            }
            ps.execute();
        }
    }

    public List<PgSqlBatchItem<Table,?>> executeCreateTables(Connection c, List<PgSqlBatchItem<Table,?>> createList) throws SQLException {

        List<PgSqlBatchItem<Table, ?>> statuses = new LinkedList<>();

        List<Table> listOfTables = PgSqlBatchItem.asListOfInputs(createList);
        Map<Table, PgSqlBatchItem> itemMap = PgSqlBatchItem.asMapOfInputs(createList);

        Catalog tempCat = new Catalog(catalog, listOfTables);
        // check that all the referred are also in createList / catalog
        // if not - don't even start creating
        for (Table t: listOfTables) {
            deps:
            for (ForeignKey fk: t.foreignKeys()) {
                Table dep = tempCat.table(fk.tableRef());
                if (dep == null) {
                    itemMap.get(t).error(new ResourceProcessingException(ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE,
                            "Table " + t.quotedSchemaName() + " has dependency on " + fk.tableRef().quotedSchemaName() + " which should also be included for creation"));
                }
            }
        }

        // now order by referring
        Set<Table> sorted = tempCat.orderByReferring(listOfTables);

        for (Table t: sorted) {
            PgSqlBatchItem<Table, ?> item = itemMap.get(t);
            statuses.add(item);

            // if item is already marked as erred, skip it
            if (item.error() != null) {
                continue;
            }
            try {
                executeCreateTable(c, tempCat.table(t.tableRef()));
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("Exception creating a table: ", e);
                }
                item.error(new ResourceProcessingException(
                        ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, e.getMessage(), e));
            }
        }
        return statuses;
    }

    public QueryResults querySelectFromTable(Connection con, Table table, Sorting sorting, Pagination pagination, String query) throws IOException, SQLException {
        // if query can't be parsed to JSON throw exception
        JsonNode q = parseJson(query);

        if (!q.isObject()) {
            throw new IllegalArgumentException("Invalid query: not an object (" + query + ")");
        }

        // convert Mongo query to SQL WHERE expression
        Expression expression = parseRelational(q);

        PreparedStatement ps = prepareSelectFromTableWhere(con, table, expression, sorting, pagination);
        return query(ps, pagination);
    }

    private PreparedStatement prepareSelectFromTableWhere(Connection con, Table table, Expression expression, Sorting sorting, Pagination pagination) throws SQLException {
        List<Pair<Key, Key>> joins = new LinkedList<>();

        Column[] col = new Column[1];
        List<Pair<Column, Value>> values = new LinkedList<>();

        // identify columns / tables in expression

        new ExpressionWalker().traverse(expression, (node) -> {

            if (node instanceof Identifier) {
                String name = ((Identifier) node).name();
                String [] segments = name.split("\\.");
                Table lastTable = table;
                Column c = null;
                int i = 0;
                for (String segment: segments) {
                    c = lastTable.column(segment);
                    if (c == null) {
                        // it might be a reference to related table
                        ForeignKey fk = lastTable.foreignKeyForFieldName(segment);
                        Table colTable = fk != null ? catalog.table(fk.tableRef()) : null;
                        if (colTable == null) {
                            colTable = catalog.table(new TableRef(segment));
                        }
                        if (colTable == null) {
                            throw new IllegalArgumentException("Query refers to non-existent field: " + name + " ('" + segment + "' not found)");
                        }
                        Key leftKey = lastTable.joinKeyForTable(colTable);
                        Key rightKey = colTable.joinKeyForTable(lastTable);
                        if (leftKey == null || rightKey == null) {
                            throw new RuntimeException("Unable to join tables for: " + name + " (left key: " + leftKey + ", right key: " + rightKey + ")");
                        }
                        lastTable = colTable;

                        joins.add(new Pair(leftKey, rightKey));
                    } else if (i != segments.length - 1) {
                        throw new IllegalArgumentException("Query refers to non-existent field: " + name + " (good up to '" + segment + "')");
                    }
                    i++;
                }
                if (c == null) {
                    throw new IllegalArgumentException("Query refers to invalid field name: " + name);
                }
                col[0] = c;

                // convert ORM scoped field name to table scoped column name
                ((Identifier) node).name(lastTable.quotedSchemaName() + "." + c.name());

            } else if (node instanceof Value) {
                values.add(new Pair<>(col[0], (Value) node));
            }
        });

        // prepare join part of the query
        StringBuilder select = new StringBuilder()
                .append(selectJoinTables(table, joins))
                .append(" WHERE ")
                .append(expression.toString());

        // TODO: finish implementing sorting
        if (sorting != null) {
            // need to convert sorting field specs to fully qualified column names
        }

        if (pagination != null) {
            if (pagination.offset() > 0) {
                select.append(" OFFSET " + pagination.offset());
            }
            select.append(" LIMIT " + pagination.limit());
        }

        PreparedStatement ps = con.prepareStatement(select.toString());

        // bind values
        int i = 1;
        for (Pair<Column, Value> pair: values) {
            pair.key().bindValue(ps, i, pair.value().value());
            i++;
        }

        return ps;
    }

    private String selectJoinTables(Table table, List<Pair<Key, Key>> joins) {
        StringBuilder sb = new StringBuilder("SELECT " + table.quotedSchemaName() + ".* FROM " + table.quotedSchemaName());

        // track processed joins to avoid duplicates
        HashSet<Pair<Key, Key>> processed = new HashSet<>();

        // We assume that joins are properly ordered so any transitive intermediaries
        // have already been taken care of by pairs earlier in the list
        for (Pair<Key, Key> pair: joins) {

            if (processed.contains(pair)) {
                continue;
            }
            List<Column> cols = pair.key().columns();
            List<Column> cols2 = pair.value().columns();

            if (cols.size() != cols2.size()) {
                throw new RuntimeException("Join columns mismatch: " + pair.key() + " vs. " + pair.value());
            }
            for (int i = 0; i < cols.size(); i++) {
                Table tab = catalog.table(cols.get(i).tableRef());
                Table tab2 = catalog.table(cols2.get(i).tableRef());
                if (i == 0) {
                    sb.append(" JOIN " + tab2.quotedSchemaName() + " ON ");
                } else {
                    sb.append(" AND ");
                }
                sb.append(tab.quotedSchemaName() + "." + cols.get(i).name() + "=" + tab2.quotedSchemaName() + "." + cols2.get(i).name());
            }
            processed.add(pair);
        }

        return sb.toString();
    }

    private Expression parseRelational(JsonNode parent) {
        if (!parent.isObject()) {
            throw new IllegalArgumentException("Invalid query: not an object (" + parent + ")");
        }

        And root = new And();
        Iterator<String> it = parent.fieldNames();
        while (it.hasNext()) {
            String name = it.next();
            if (name.startsWith("$")) {
                // it's an operator
                JsonNode node = parent.get(name);
                Operator op;
                switch (name) {
                    case "$or":
                        op = new Or();
                        parseLogical((LogicalOperator) op, node);
                        break;
                    case "$and":
                        op = new And();
                        parseLogical((LogicalOperator) op, node);
                        break;
                    case "$gt":
                        op = new GreaterThan();
                        op.right(parseRelationalOperand(node));
                        break;
                    case "$lt":
                        op = new LessThan();
                        op.right(parseRelationalOperand(node));
                        break;
                    case "$gte":
                        op = new GreaterThanOrEqual();
                        op.right(parseRelationalOperand(node));
                        break;
                    case "$lte":
                        op = new LessThanOrEqual();
                        op.right(parseRelationalOperand(node));
                        break;
                    case "$ne":
                        op = new NotEqual();
                        op.right(parseRelationalOperand(node));
                        break;
                    case "$not":
                        op = new Not();
                        parseLogical((LogicalOperator) op, node);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + name);
                }
                root.next(op);
            } else {
                // it's a field name
                JsonNode node = parent.get(name);
                if (node.isObject()) {
                    Expression op = parseRelational(node).normalize();
                    if (op instanceof RelationalOperator) {
                        ((RelationalOperator) op).left(new Identifier(name));
                    }
                    root.next(op);
                } else {
                    try {
                        RelationalOperand value = parseRelationalOperand(node);
                        root.next(new Identifier(name).equalTo(value));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unexpected value of " + name + ": " + node);
                    }
                }
            }
        }
        return root.normalize();
    }

    private RelationalOperand parseRelationalOperand(JsonNode node) {
        Object value;
        if (node.isTextual()) {
            value = node.textValue();
        } else if (node.isBoolean()) {
            value = node.booleanValue();
        } else if (node.isInt()) {
            value = node.intValue();
        } else if (node.isLong()) {
            value = node.longValue();
        } else if (node.isBigInteger()) {
            value = node.bigIntegerValue();
        } else if (node.isFloat()) {
            value = node.floatValue();
        } else if (node.isDouble()) {
            value = node.doubleValue();
        } else if (node.isBigDecimal()) {
            value = node.decimalValue();
        } else if (node.isNull()) {
            value = null;
        } else {
            throw new IllegalArgumentException("Unexpected value of: " + node);
        }
        return new Value(value);
    }

    private void parseLogical(LogicalOperator op, JsonNode node) {

        // if node is array iterate over items, convert them to Expressions, and attach them to op ...
        if (node.isArray()) {
            for (JsonNode el : node) {
                op = op.next(parseRelational(el));
            }
        } else {
            op.next(parseRelational(node));
        }
    }

    private JsonNode parseJson(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper(JSON_FACTORY);
        JsonParser jp = JSON_FACTORY.createParser(jsonString);
        return mapper.readTree(jp);
    }

    private static String rawSQL(PreparedStatement ps) {
        String raw = ps.toString();
        int len = "Pooled statement wrapping physical statement ".length();
        if (raw.length() > len) {
            raw = raw.substring(len);
        }
        return raw;
    }
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    static {
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JSON_FACTORY.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }
}
