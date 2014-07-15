package io.liveoak.pgsql.meta;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class Column {

    TableRef table;
    String name;
    String type;
    int size;
    boolean notNull;
    boolean unique;

    public Column(TableRef table, String name, String type, int size, boolean notNull, boolean unique) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.size = size;
        this.notNull = notNull;
        this.unique = unique;
    }

    public TableRef tableRef() {
        return table;
    }

    public String name() {
        return name;
    }

    public String quotedName() {
        return "\"" + name + "\"";
    }

    public String type() {
        return type;
    }

    public int size() {
        return size;
    }

    public String typeSpec() {
        switch (type) {
            case "char":
            case "varchar":
                return type + " (" + size + ")";
            default:
                return type;
        }
    }

    public boolean unique() {
        return unique;
    }

    public boolean notNull() {
        return notNull;
    }

    public void bindValue(PreparedStatement ps, int i, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(i, sqlType());
        } else {
            switch (type) {
                case "int4":
                    ps.setInt(i, ValueConverter.toInt(value));
                    return;
                default:
                    ps.setObject(i, value);
            }
/*
                case "varchar":
                    return Types.VARCHAR;
                case "nvarchar":
                    return Types.NVARCHAR;
                case "char":
                    return Types.CHAR;
                case "nchar":
                    return Types.NCHAR;
                case "binary":
                    return Types.BINARY;
                case "varbinary":
                    return Types.VARBINARY;

*/

        }
    }

    public int sqlType() {
        switch (type) {
            case "int8":
                return Types.INTEGER;
            case "varchar":
                return Types.VARCHAR;
            case "nvarchar":
                return Types.NVARCHAR;
            case "char":
                return Types.CHAR;
            case "nchar":
                return Types.NCHAR;
            case "binary":
                return Types.BINARY;
            case "varbinary":
                return Types.VARBINARY;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }
}