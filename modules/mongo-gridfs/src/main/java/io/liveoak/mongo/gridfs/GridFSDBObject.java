/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.Date;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class GridFSDBObject {

    private DBObject obj;

    public GridFSDBObject(DBObject obj) {
        this.obj = obj;
    }

    public ObjectId getId() {
        return (ObjectId) obj.get("_id");
    }

    public ObjectId getParentId() {
        return (ObjectId) obj.get("parent");
    }

    public String getParentIdAsString() {
        Object parent = obj.get("parent");
        return parent == null ? null : parent.toString();
    }

    public String getString(String name) {
        Object val = obj.get(name);
        return val == null ? null : val.toString();
    }

    public Long getLong(String name) {
        Object val = obj.get(name);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            return Long.parseLong((String) val);
        } else if (val == null) {
            return null;
        } else {
            throw new NumberFormatException("Not a Long (for key '" + name + "'): " + val);
        }
    }

    public boolean isTrue(String name) {
        Object val = obj.get(name);
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean == false) {
            return false; // throw assert exception?
        }
        return ((Boolean) val).booleanValue();
    }

    public Object get(String name) {
        return obj.get(name);
    }

    public DBObject dbObject() {
        return obj;
    }

    public Date getDate(String name) {
        Object val = obj.get(name);
        if (val == null) {
            return null;
        }
        if (val instanceof Date == false) {
            throw new RuntimeException("Property name not of type Date: " + val);
        }
        return (Date) val;
    }

    public Long getDateAsMillis(String name) {
        Date val = getDate(name);
        return val == null ? null : val.getTime();
    }
}
