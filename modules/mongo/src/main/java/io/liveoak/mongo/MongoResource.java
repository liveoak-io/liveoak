/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.Resource;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class MongoResource implements Resource, BlockingResource {

    private MongoResource parent;

    protected static final String MONGO_ID_FIELD = "_id";
    protected static final String MBAAS_ID_FIELD = "id";

    //TODO: see if there is a more elegant way to handle this. Prefixes are lame....
    protected static final String MBAAS_MONGO_OBJECT_ID_PREFIX = "_mOI:";

    public MongoResource(MongoResource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    protected DBObject getMongoID(String liveOakID) {
        if (liveOakID.startsWith( MBAAS_MONGO_OBJECT_ID_PREFIX)) {
            String id = liveOakID.substring( MBAAS_MONGO_OBJECT_ID_PREFIX.length() );
            return new BasicDBObject(MONGO_ID_FIELD, new ObjectId( id ));
        } else {
            return new BasicDBObject(MONGO_ID_FIELD, liveOakID);
        }
    }

    protected String getResourceID(DBObject mongoObject) {
        Object id = mongoObject.get( MONGO_ID_FIELD );

        if (id instanceof ObjectId) {
            return MBAAS_MONGO_OBJECT_ID_PREFIX + id.toString();
        } else {
            return id.toString();
        }
    }
}
