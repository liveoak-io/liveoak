/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class MongoObjectResource extends MongoResource {

    protected DBObject dbObject;

    public MongoObjectResource(MongoResource parent, DBObject dbObject) {
        super(parent);
        this.dbObject = dbObject;
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        Object object = getDBObject().get(id);
        if (object != null) {
            if (object instanceof BasicDBObject || object instanceof BasicDBList) {
                return null;
            } else if (object instanceof DBRef) {
                //try {
                // TODO throw exception that will end up as InvalidRequest
                return getResource((DBRef) object, ctx.returnFields().child(id).isEmpty());
                //} catch (ResourceProcessingException e) {
                //    responder.invalidRequest(e.getMessage());
                //}
            } else {
                // TODO throw exception that will end up as InternalError
                //responder.internalError("ERROR: Object type (" + object.getClass() + ") not recognized");
                throw new RuntimeException("ERROR: Object type (" + object.getClass() + ") not recognized");
            }
        }
        return null;
    }

    @Override
    public ResourceState properties(RequestContext ctx) throws Exception {
        // TODO: only read properties specified in the return fields and not everything
        ResourceState result = new DefaultResourceState();
        Set<String> keys = getDBObject().keySet();
        for (String key : keys) {
            if (!key.equals(MONGO_ID_FIELD) && !key.equals(LiveOak.ID)) {
                Object value = getDBObject().get(key);
                if (value instanceof BasicDBObject) {
                    value = new MongoEmbeddedObjectResource(this, (DBObject) value);
                } else if (value instanceof BasicDBList) {
                    value = getResourceCollection(value);
                } else if (value instanceof DBRef) {
                    value = getResource((DBRef) value, ctx.returnFields().child(key).isEmpty());
                }

                if (supportedObject(value)) {
                    result.putProperty(key, value);
                } else {
                    log.warn("Unsupported Property type " + value.getClass() + " cannot encode.");
                }
            }
        }
        return result;
    }

    protected boolean supportedObject(Object object) {
        if (object instanceof Resource ||
                object instanceof String ||
                object instanceof Boolean ||
                object instanceof Integer ||
                object instanceof Double ||
                object instanceof Long ||
                object instanceof Date ||
                object instanceof Boolean ||
                object instanceof Collection ||
                object == null) {
            return true;
        } else {
            return false;
        }
    }

    protected Object getResourceCollection(Object object) throws Exception {
        if (object instanceof BasicDBObject) {
            return new MongoEmbeddedObjectResource(this, (DBObject) object);
        } else if (object instanceof BasicDBList) {
            BasicDBList dbList = ((BasicDBList) object);
            ArrayList list = new ArrayList();
            for (int i = 0; i < dbList.size(); i++) {
                Object child = dbList.get(i);
                list.add(getResourceCollection(child));
            }
            return list;
        } else if (object instanceof DBRef) {
            return getResource((DBRef) object, true);
        } else {
            return object;
        }
    }

    public String toString() {
        return "[" + this.getClass() + ": obj=" + getDBObject() + "]";
    }

    @Override
    public String id() {
        return getResourceID(getDBObject());
    }

    protected DBObject getDBObject() {
        return dbObject;
    }
}
