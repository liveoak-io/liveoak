package io.liveoak.pgsql.meta;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class ValueConverter {

    public static int toInt(Object val) {
        if (val == null) {
            throw new IllegalArgumentException("val == null");
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else {
            return Integer.parseInt(String.valueOf(val));
        }
    }
}
