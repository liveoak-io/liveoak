package io.liveoak.pgsql.meta;

import java.util.List;

public class ForeignKey extends Key {
    private TableRef table;

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
}