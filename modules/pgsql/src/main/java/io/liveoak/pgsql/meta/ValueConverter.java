/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.meta;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            return parseIsoDateTime((String) val);
        }
        if (val instanceof Timestamp) {
            return (Timestamp) val;
        }
        if (val instanceof Date) {
            return new Timestamp(((Date) val).getTime());
        }
        throw new IllegalArgumentException("Unsupported type: " + val.getClass());
    }

    private static Timestamp parseIsoDateTime(String val) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(val, dtf);
            Timestamp.valueOf(ldt);
        } catch (DateTimeParseException ignored) {}

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(val, dtf);
            Timestamp.valueOf(ldt);
        } catch (DateTimeParseException ignored) {}

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime ldt = LocalDateTime.parse(val, dtf);
            Timestamp.valueOf(ldt);
        } catch (DateTimeParseException ignored) {}

        throw new IllegalArgumentException("Value could not be parsed as ISO DateTime: " + val);
    }
}
