/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.meta;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static Timestamp toTimestamp(Object val) {
        if (val == null) {
            throw new IllegalArgumentException("val == null");
        }
        if (val instanceof Long || val instanceof Integer) {
            return new Timestamp(((Number) val).longValue());
        }
        if (val instanceof String) {
            return new Timestamp(parseIsoDateTime((String) val).getTime());
        }
        if (val instanceof Timestamp) {
            return (Timestamp) val;
        }
        if (val instanceof Date) {
            return new Timestamp(((Date) val).getTime());
        }
        throw new IllegalArgumentException("Unsupported type: " + val.getClass());
    }

    private static Date parseIsoDateTime(String val) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(val);
        } catch (ParseException ignored) {}

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(val);
        } catch (ParseException ignored) {}

        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(val);
        } catch (ParseException ignored) {}

        throw new IllegalArgumentException("Value could not be parsed as ISO DateTime: " + val);
    }
}
