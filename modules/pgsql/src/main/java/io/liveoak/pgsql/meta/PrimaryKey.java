package io.liveoak.pgsql.meta;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.pgsql.data.Row;

public class PrimaryKey extends Key {
    public PrimaryKey(List<Column> cols) {
        super(cols);
    }

    public static String[] splitId(String value) {
        return value.split("\\.");
    }

    public static List<String> splitIdAsList(String value) {
        return Arrays.asList(splitId(value));
    }

    public static String spliceId(List<Object> ids) {
        return spliceId(ids.toArray(new String[ids.size()]));
    }

    public static String spliceId(String[] ids) {
        StringBuilder sb = new StringBuilder();
        for (String v: ids) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(v);
        }
        return sb.toString();
    }

    public String idFromRow(Row row) {
        List<Object> vals = new LinkedList<>();
        for (Column c: columns()) {
            Object val = row.value(c.name());
            if (val == null) {
                throw new RuntimeException("Key column value should not be null: " + c.name());
            }
            vals.add(val);
        }
        return PrimaryKey.spliceId(vals);
    }
}