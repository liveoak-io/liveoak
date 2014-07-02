/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.state;

import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Opaque state of a resource.
 *
 * <p>State objects are used to instill new state into a server-side resource.</p>
 *
 * @author Bob McWhirter
 */
public interface ResourceState {

    /**
     * Retrieve the ID of the resource.
     *
     * @return The ID of the resource.
     */
    String id();

    /**
     * Set the ID of the resource.
     *
     * @param id The ID of the resource.
     */
    void id(String id);

    void uri(URI uri);

    URI uri();

    /**
     * Add a property to the state.
     *
     * <p>Property values may be either simple scalar
     * values, or complex {@link ResourceState} objects</p>
     *
     * @param name  The name of the property.
     * @param value The value of the property.
     */
    void putProperty(String name, Object value);

    /**
     * Retrieve a property value.
     *
     * @param name The property name.
     * @return The value of the property, as either a simple scalar, or as a
     *         more complex {@link ResourceState}.
     */
    Object getProperty(String name);

    /**
     * Retreive a property value as String
     *
     * @param name The property name.
     * @return The value of the property, as a String
     * @throw RuntimeException if value of the named property is not a String
     */
    default String getPropertyAsString(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof String || val instanceof Number || val instanceof Boolean) {
            return String.valueOf(val);
        }
        throw new RuntimeException("Value can't be returned as String: " + val + " [" + val.getClass() + "]");
    }

    default Integer getPropertyAsInteger(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Integer || val instanceof Long || val instanceof Short) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            return Integer.valueOf((String) val);
        }
        throw new RuntimeException("Value can't be returned as Integer: " + val + " [" + val.getClass() + "]");
    }

    default Long getPropertyAsLong(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Integer || val instanceof Long || val instanceof Short) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            return Long.valueOf((String) val);
        }
        throw new RuntimeException("Value can't be returned as Long: " + val + " [" + val.getClass() + "]");
    }

    default Boolean getPropertyAsBoolean(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        }
        if (val instanceof String) {
            return Boolean.valueOf((String) val);
        }
        throw new RuntimeException("Value can't be returned as Boolean: " + val + " [" + val.getClass() + "]");
    }

    default Date getPropertyAsDate(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Date || val instanceof Timestamp) {
            return (Date) val;
        }
        if (val instanceof Calendar) {
            return ((Calendar) val).getTime();
        }
        if (val instanceof Long) {
            return new Date(((Long) val).longValue());
        }
        if (val instanceof String) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                return sdf.parse((String) val);
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Value can't be returned as Date: " + val + " [" + val.getClass() + "]");
    }

    default boolean isListPropertyOrNull(String name) {
        Object val = getProperty(name);
        return val == null || val instanceof List;
    }

    default List getPropertyAsList(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof List) {
            return (List) val;
        }

        ArrayList ret = new ArrayList();
        ret.add(val);
        return ret;
    }

    default ResourceState getPropertyAsResourceState(String name) {
        Object val = getProperty(name);
        if (val == null) {
            return null;
        }
        if (val instanceof ResourceState) {
            return (ResourceState) val;
        }
        throw new RuntimeException("Value can't be returned as ResourceState: " + val + " [" + val.getClass() + "]");
    }

    Object removeProperty(String name);

    Set<String> getPropertyNames();

    void addMember(ResourceState member);

    List<ResourceState> members();


}
