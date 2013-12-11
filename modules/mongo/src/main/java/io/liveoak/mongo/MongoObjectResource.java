/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;

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
    public void readMember(RequestContext ctx, String id, Responder responder) {
        Object object = getDBObject().get(id);
        if (object != null) {
            if (object instanceof BasicDBObject || object instanceof BasicDBList) {
                responder.noSuchResource(id);
            } else if (object instanceof DBRef) {
                try {
                    responder.resourceRead(getResource((DBRef) object, ctx.returnFields().child(id).isEmpty()));
                } catch (ResourceProcessingException e) {
                    responder.invalidRequest(e.getMessage());
                }
            } else {
                responder.internalError("ERROR: Object type (" + object.getClass() + ") not recognized");
            }
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        // TODO: only read properties specified in the return fields and not everything
        Set<String> keys = getDBObject().keySet();
        for (String key : keys) {
            if (!key.equals(MONGO_ID_FIELD) && !key.equals(MBAAS_ID_FIELD)) {
                Object value = getDBObject().get(key);
                if (value instanceof BasicDBObject) {
                    value = new MongoEmbeddedObjectResource(this, (DBObject) value);
                } else if (value instanceof BasicDBList) {
                    value = getResourceCollection(value);
                } else if (value instanceof DBRef) {
                    value = getResource((DBRef) value, ctx.returnFields().child(key).isEmpty());
                }
                sink.accept(key, value);
            }
        }
        sink.close();
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
