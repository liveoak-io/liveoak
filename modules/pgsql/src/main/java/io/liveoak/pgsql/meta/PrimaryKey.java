package io.liveoak.pgsql.meta;

import java.util.Arrays;
import java.util.List;

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
}