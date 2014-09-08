/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Collection;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class MongoResource implements Resource, BlockingResource {

    private MongoResource parent;

    protected static final String MONGO_ID_FIELD = "_id";

    // TODO: see if there is a more elegant way to handle this. Prefixes are lame....
    protected static final String MBAAS_MONGO_OBJECT_ID_PREFIX = "ObjectId(\"";
    protected static final String MBAAS_MONGO_OBJECT_ID_SUFFIX = "\")";

    protected static final Logger log = Logger.getLogger("io.liveoak.mongo");

    public MongoResource(MongoResource parent) {
        this.parent = parent;
    }

    public Logger logger() {
        return log;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    protected DBObject getMongoID(String liveOakID) {
        if (liveOakID.startsWith(MBAAS_MONGO_OBJECT_ID_PREFIX) && liveOakID.endsWith(MBAAS_MONGO_OBJECT_ID_SUFFIX)) {
            String id = liveOakID.substring(MBAAS_MONGO_OBJECT_ID_PREFIX.length(), liveOakID.length() - MBAAS_MONGO_OBJECT_ID_SUFFIX.length());
            return new BasicDBObject(MONGO_ID_FIELD, new ObjectId(id));
        } else {
            return new BasicDBObject(MONGO_ID_FIELD, liveOakID);
        }
    }

    protected String getResourceID(DBObject mongoObject) {
        Object id = mongoObject.get(MONGO_ID_FIELD);

        return getResourceID(id);
    }

    protected String getResourceID(Object id) {
        if (id instanceof ObjectId) {
            return MBAAS_MONGO_OBJECT_ID_PREFIX + id.toString() + MBAAS_MONGO_OBJECT_ID_SUFFIX;
        } else {
            return id.toString();
        }
    }

    protected MongoObjectResource getResource(DBRef dbRef, boolean byReference) throws ResourceProcessingException {
        return parent.getResource(dbRef, byReference);
    }

    protected DBRef getDBRef(String uri) throws ResourceProcessingException {
        return parent.getDBRef(uri);
    }

    protected BasicDBObject createObject(ResourceState resourceState) throws Exception {
        BasicDBObject basicDBObject = new BasicDBObject();

        // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
        String rid = resourceState.id();
        if (rid != null) {
            basicDBObject.append(MONGO_ID_FIELD, rid);
        }

        Set<String> keys = resourceState.getPropertyNames();

        for (String key : keys) {
            if (key.equalsIgnoreCase("$dbref")) {
                DBRef dbRef = getDBRef((String) resourceState.getProperty("$dbref"));
                basicDBObject.append(key, dbRef);
            } else if (!key.equals(LiveOak.ID)) { // don't append the ID field again
                Object value = resourceState.getProperty(key);
                if (value instanceof ResourceState) {
                    Object dbrefObject = ((ResourceState) value).getProperty("$dbref");
                    if (dbrefObject != null) {
                        String uri = (String) dbrefObject;
                        value = getDBRef(uri);
                    } else {
                        value = createObject((ResourceState) value);
                    }
                } else if (value instanceof Collection) {
                    value = createObjectList((Collection) value);
                }
                basicDBObject.append(key, value);
            }
        }

        return basicDBObject;
    }

    protected BasicDBList createObjectList(Collection collection) throws Exception {
        BasicDBList dbList = new BasicDBList();
        for (Object object : collection) {
            if (object instanceof ResourceState) {
                dbList.add(createObject((ResourceState) object));
            } else if (object instanceof Collection) {
                dbList.add(createObjectList((Collection) object));
            } else {
                dbList.add(object);
            }
        }
        return dbList;
    }

}
