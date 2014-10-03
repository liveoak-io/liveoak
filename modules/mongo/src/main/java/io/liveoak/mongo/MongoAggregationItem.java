/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoAggregationItem extends MongoObjectResource {

    public MongoAggregationItem(MongoResource parent, DBObject obj) {
        super(parent, obj);
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        Set<String> keys = this.dbObject.keySet();
        for (String key : keys) {
            Object value = this.dbObject.get(key);
            if (value instanceof BasicDBObject) {
                value = new MongoAggregationItem(this, (DBObject) value);
            } else if (value instanceof BasicDBList) {
                value = getResourceCollection(value);
            }

            if (supportedObject(value)) {
                result.put(key, value);
            } else {
                log.warn("Unsupported Property type: " + value.getClass() + " cannot encode.");
            }
        }
        return result;
    }

    @Override
    protected Object getResourceCollection(Object object) {
        if (object instanceof BasicDBObject) {
            return new MongoAggregationItem(this, (DBObject) object);
        } else if (object instanceof BasicDBList) {
            BasicDBList dbList = ((BasicDBList) object);
            ArrayList list = new ArrayList();
            for (int i = 0; i < dbList.size(); i++) {
                Object child = dbList.get(i);
                list.add(getResourceCollection(child));
            }
            return list;
        } else {
            return object;
        }
    }
}
