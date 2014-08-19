package io.liveoak.pgsql.meta;

import java.util.List;

public class ForeignKey extends Key {
    private TableRef table;
    private Catalog catalog;
    private String fieldName;

    private ForeignKey(List<Column> fkCols) {
        super(fkCols);
    }

    public ForeignKey(List<Column> fkCols, TableRef table) {
        super(fkCols);
        if (table == null) {
            throw new IllegalArgumentException("table == null");
        }
        this.table = table;
    }

    public TableRef tableRef() {
        return table;
    }

    void catalog(Catalog catalog) {
        if (this.catalog != null) {
            throw new RuntimeException("Catalog already set");
        }
        this.catalog = catalog;

        if (fieldName == null) {
            String name = columns().get(0).name();
            Table table = catalog.table(tableRef());
            String tableId = table != null ? table.id() : tableRef().schemaName();

            if (name.endsWith("_id")) {
                fieldName = name.substring(0, name.length()-3);
            } else {
                fieldName = tableId;
            }
        }
    }

    public String fieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ForeignKey that = (ForeignKey) o;

        if (!table.equals(that.table)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + table.hashCode();
        return result;
    }

    public ForeignKey copy() {
        ForeignKey fk = new ForeignKey(columns());
        fk.fieldName = fieldName;
        fk.table = table;
        return fk;
    }
}