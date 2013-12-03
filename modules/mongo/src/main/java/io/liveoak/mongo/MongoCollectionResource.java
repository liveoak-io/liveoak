/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class MongoCollectionResource extends MongoResource {

    DBCollection dbCollection;

    MongoCollectionResource(MongoResource parent, DBCollection collection) {
        super(parent);
        this.dbCollection = collection;
    }

    @Override
    public void readMember(RequestContext ctx, String childId, Responder responder) {

        if ("_aggregate".equals(childId)) {
            responder.resourceRead(new MongoAggregationResource(this));
            return;
        }

        DBObject object = dbCollection.findOne( getMongoID( childId ) );

        if (object == null) {
            responder.noSuchResource(childId);
        } else {
            responder.resourceRead(new MongoBaseObjectResource(this, object));
        }
    }

    @Override
    public String id() {
        return dbCollection.getName();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        dbCollection.drop();
        responder.resourceDeleted(this);
    }

    protected WriteResult deleteChild(RequestContext ctx, String childId) {
        WriteResult wResult = null;
        wResult = dbCollection.remove( getMongoID( childId ) );
        return wResult;
    }

    protected Object updateChild(RequestContext ctx, String childId, Object child) {
        if (child instanceof DBObject) {
            DBObject childObject = (DBObject) child;
            WriteResult wResult = dbCollection.update(new BasicDBObject(MONGO_ID_FIELD, childObject.get(MONGO_ID_FIELD)), childObject);
            return wResult;
        } else {
            throw new RuntimeException("ERROR"); //TODO: fix this
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        DBObject queryObject = new BasicDBObject();

        ResourceParams resourceParams = ctx.resourceParams();
        if (resourceParams != null) {
            if (resourceParams.contains("q")) {
                String queryString = ctx.resourceParams().value("q");
                try {
                    queryObject = (DBObject) JSON.parse(queryString);
                } catch (Exception e) {
                    throw new ResourceProcessingException("Invalid JSON format for the 'query' parameter", e);
                }
            }
        }

        DBObject returnFields = new BasicDBObject();
        if (ctx.returnFields() != null && !ctx.returnFields().isAll()) {
            ctx.returnFields().forEach((fieldName) -> {
                returnFields.put(fieldName, true);
            });
        }

        DBCursor dbCursor = dbCollection.find(queryObject, returnFields);

        if (ctx.resourceParams() != null && ctx.resourceParams().contains("hint")) {
            String hint = ctx.resourceParams().value( "hint" );
            if (hint.startsWith("{")) {
                try {
                    DBObject hintObject = (DBObject) JSON.parse(hint);
                    dbCursor.hint(hintObject) ;
                } catch (Exception e) {
                    throw new ResourceProcessingException("Invalid JSON format for the 'hint' parameter", e);
                }
            } else {
                dbCursor.hint(hint);
            }
        }

        if (resourceParams != null && ctx.resourceParams().contains("explain")) {
            if (ctx.resourceParams().value("explain").equalsIgnoreCase("true")) {
                sink.accept( new MongoEmbeddedObjectResource(this, dbCursor.explain()));
                sink.close();
                return;
            }
        }

        Sorting sorting = ctx.sorting();
        if (sorting != null) {
            BasicDBObject sortingObject = new BasicDBObject();
            for (Sorting.Spec spec : sorting) {
                sortingObject.append(spec.name(), spec.ascending() ? 1 : -1);
            }
            dbCursor = dbCursor.sort(sortingObject);
        }

        if (ctx.pagination() != null) {
            dbCursor.limit(ctx.pagination().limit());
            dbCursor.skip(ctx.pagination().offset());
        }

        try {
            dbCursor.hasNext();
        } catch (Exception e) {
            throw new ResourceProcessingException("Exception encountered trying to fetch data from the Mongo Database", e);
        }

        dbCursor.forEach((dbObject) -> {
            sink.accept(new MongoBaseObjectResource(this, dbObject));
        });

        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //TODO: properly handle errors
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = (BasicDBObject) createObject(state);
            WriteResult wResult = dbCollection.insert(basicDBObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DBObject newDBObject = dbCollection.findOne(new BasicDBObject(MONGO_ID_FIELD, basicDBObject.get(MONGO_ID_FIELD)));
        responder.resourceCreated(new MongoBaseObjectResource(this, newDBObject));
    }

    public String toString() {
        return "[MongoCollectionResource: id=" + this.id() + "]";
    }

    protected BasicDBObject createObject(ResourceState resourceState) {
        BasicDBObject basicDBObject = new BasicDBObject();

        // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
        String rid = resourceState.id();
        if (rid != null) {
            basicDBObject.append(MONGO_ID_FIELD, rid);
        }

        Set<String> keys = resourceState.getPropertyNames();

        for (String key : keys) {
            if (!key.equals(MBAAS_ID_FIELD)) { //don't append the ID field again
                Object value = resourceState.getProperty(key);
                if (value instanceof ResourceState) {
                    value = createObject((ResourceState) value);
                } else if (value instanceof Collection) {
                    value = createObjectList((Collection) value);
                }
                basicDBObject.append(key, value);
            }
        }

        return basicDBObject;
    }

    protected BasicDBList createObjectList(Collection collection) {
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

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("type", "collection");
        sink.close();
    }

}
