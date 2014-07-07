package io.liveoak.pgsql.meta;

import java.util.List;

public class ForeignKey extends Key {
    private TableRef table;

    public ForeignKey(List<Column> fkCols, TableRef table) {
        super(fkCols);
        this.table = table;
    }

    public TableRef tableRef() {
        return table;
    }
}