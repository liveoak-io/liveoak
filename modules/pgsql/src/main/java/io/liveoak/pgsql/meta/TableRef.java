package io.liveoak.pgsql.meta;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class TableRef {
    private String schema;
    private String table;

    public TableRef(String schema, String table) {
        this.schema = schema;
        this.table = table;
    }

    public TableRef(String fullName) {
        String [] pair = fullName.split("\\.");
        if (pair.length == 2) {
            schema = pair[0];
            table = pair[1];
        } else if (pair.length == 1) {
            table = pair[0];
        } else {
            throw new IllegalArgumentException("Invalid table id: " + fullName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableRef tableRef = (TableRef) o;

        if (schema != null ? !schema.equals(tableRef.schema) : tableRef.schema != null) return false;
        if (!table.equals(tableRef.table)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = schema != null ? schema.hashCode() : 0;
        result = 31 * result + table.hashCode();
        return result;
    }

    public String schema() {
        return schema;
    }

    public String name() {
        return table;
    }


    public String quotedSchema() {
        return "\"" + schema + "\"";
    }

    public String asQuotedIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        if (schema != null) {
            sb.append(schema).append("\".\"");
        }
        sb.append(table).append("\"");
        return sb.toString();
    }

    public String asUnquotedIdentifier() {
        StringBuilder sb = new StringBuilder();
        if (schema != null) {
            sb.append(schema).append('.');
        }
        sb.append(table);
        return sb.toString();
    }
}