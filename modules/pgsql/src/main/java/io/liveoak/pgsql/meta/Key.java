package io.liveoak.pgsql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Key {
    private List<Column> cols;

    Key(List<Column> cols) {
        if (cols == null) {
            this.cols = Collections.emptyList();
        } else {
            this.cols = Collections.unmodifiableList(new ArrayList(cols));
        }
    }

    public boolean isEmpty() {
        return cols.isEmpty();
    }

    public Column getColumn(String name) {
        for (Column c : cols) {
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }

    public List<Column> columns() {
        return cols;
    }

    public int indexForColumn(String name) {
        int i = 0;
        for (Column c : cols) {
            if (c.name().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!cols.equals(key.cols)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return cols.hashCode();
    }
}