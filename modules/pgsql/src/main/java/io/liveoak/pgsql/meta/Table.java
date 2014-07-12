package io.liveoak.pgsql.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class Table {

    private String id;
    private String schema;
    private String name;
    private List<Column> columns;
    private PrimaryKey pk;
    private List<ForeignKey> foreignKeys;

    /**
     * In all referredKeys the ForeignKey.tableRef points to this table.
     * Origin table can be retrieved from Column instances.
     */
    private List<ForeignKey> referredKeys;


    public Table(String id, Table table, List<ForeignKey> referredKeys) {
        this.id = id;
        this.schema = table.schema;
        this.name = table.name;
        this.columns = table.columns;
        this.pk = table.pk;
        this.foreignKeys = table.foreignKeys;
        this.referredKeys = referredKeys != null ? referredKeys : Collections.emptyList();
    }

    public Table(String schema, String name, List<Column> columns, PrimaryKey key, List<ForeignKey> foreignKeys) {
        this.schema = schema;
        this.name = name;
        if (columns != null) {
            for (Column c : columns) {
                c.table = new TableRef(schema, name);
            }
            this.columns = Collections.unmodifiableList(columns);
        } else {
            this.columns = Collections.emptyList();
        }
        this.pk = key;
        if (foreignKeys != null) {
            this.foreignKeys = Collections.unmodifiableList(foreignKeys);
        } else {
            this.foreignKeys = Collections.emptyList();
        }
    }

    public Column column(String colName) {
        for (Column col : columns) {
            if (col.name.equals(colName)) {
                return col;
            }
        }
        return null;
    }

    public String id() {
        return id;
    }

    public String schema() {
        return schema;
    }

    public String name() {
        return name;
    }

    public String schemaName() {
        StringBuilder sb = new StringBuilder();
        if (schema != null) {
            sb.append(schema).append(".");
        }
        sb.append(name);
        return sb.toString();
    }

    public String quotedSchemaName() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        if (schema != null) {
            sb.append(schema).append("\".\"");
        }
        sb.append(name).append("\"");
        return sb.toString();
    }

    public List<Column> columns() {
        return columns;
    }

    public PrimaryKey pk() {
        return pk;
    }

    public List<ForeignKey> foreignKeys() {
        return foreignKeys;
    }

    public List<ForeignKey> referredKeys() {
        return referredKeys;
    }

    public String ddl(Map<TableRef, Table> tables) {

        StringBuilder sb = new StringBuilder("CREATE TABLE \"").append(schema()).append("\".\"").append(name()).append("\" ( \n    ");
        int i = 0;
        for (Column col : columns()) {
            if (i > 0) {
                sb.append(",\n    ");
            }
            sb.append("\"").append(col.name()).append("\" ").append(col.typeSpec());
            boolean partOfPk = pk().getColumn(col.name()) != null;
            if (col.unique() && !partOfPk) {
                sb.append(" UNIQUE ");
            }
            if (col.notNull() && !partOfPk) {
                sb.append(" NOT NULL");
            }
            i++;
        }
        List<Column> pkcols = pk().columns();
        if (pkcols.size() > 0) {
            sb.append(",\n    PRIMARY KEY (");
            i = 0;
            for (Column col : pkcols) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("\"").append(col.name()).append("\"");
                i++;
            }
            sb.append(")");
        }

        List<ForeignKey> fkeys = foreignKeys();
        for (ForeignKey fk : fkeys) {
            sb.append(",\n    FOREIGN KEY (");
            i = 0;
            for (Column col : fk.columns()) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("\"").append(col.name()).append("\"");
                i++;
            }
            sb.append(") REFERENCES \"").append(fk.tableRef().schema()).append("\".\"").append(fk.tableRef().name()).append("\" (");

            Table refTable = tables.get(fk.tableRef());
            if (refTable != null) {
                i = 0;
                for (Column col : refTable.pk().columns()) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append("\"").append(col.name()).append("\"");
                    i++;
                }
            }
            sb.append(")");
        }

        sb.append("\n)\n");
        return sb.toString();
    }

    public ForeignKey foreignKeyForColumnName(String name) {
        for (ForeignKey fk: foreignKeys) {
            for (Column c: fk.columns()) {
                if (c.name().equals(name)) {
                    return fk;
                }
            }
        }
        return null;
    }
}