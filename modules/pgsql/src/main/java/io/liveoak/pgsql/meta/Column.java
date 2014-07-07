package io.liveoak.pgsql.meta;

public class Column {

    Table table;
    String name;
    String type;
    int size;
    boolean notNull;
    boolean unique;

    public Column(Table table, String name, String type, int size, boolean notNull, boolean unique) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.size = size;
        this.notNull = notNull;
        this.unique = unique;
    }

    public String name() {
        return name;
    }

    public String quotedName() {
        return "\"" + name + "\"";
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
}