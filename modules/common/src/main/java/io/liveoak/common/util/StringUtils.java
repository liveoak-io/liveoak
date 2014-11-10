package io.liveoak.common.util;

/**
 * @author Ken Finnigan
 */
public final class StringUtils {
    private StringUtils() {
    }

    public static boolean hasValue(String value) {
        return value != null && value.length() > 0;
    }
}
