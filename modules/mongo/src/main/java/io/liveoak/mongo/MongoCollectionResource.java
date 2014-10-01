/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class MongoCollectionResource extends MongoResource {

    DBCollection dbCollection;
    String collectionName;

    MongoCollectionResource(RootMongoResource parent, DBCollection collection) {
        super(parent);
        this.dbCollection = collection;
    }

    MongoCollectionResource(MongoResource parent, String collectionName) {
        super(parent);
        this.collectionName = collectionName;
    }

    @Override
    public void readMember(RequestContext ctx, String childId, Responder responder) {

        if ("_aggregate".equals(childId)) {
            responder.resourceRead(new MongoAggregationResource(this));
            return;
        }

        DBObject object = dbCollection.findOne(getMongoID(childId));

        if (object == null) {
            responder.noSuchResource(childId);
        } else {
            responder.resourceRead(new MongoBaseObjectResource(this, object));
        }
    }

    @Override
    public String id() {
        return getDBCollection().getName();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        getDBCollection().drop();
        responder.resourceDeleted(this);
    }

    protected WriteResult deleteChild(RequestContext ctx, String childId) {
        WriteResult wResult = null;
        wResult = getDBCollection().remove(getMongoID(childId));
        return wResult;
    }

    protected Object updateChild(RequestContext ctx, String childId, Object child) {
        if (child instanceof DBObject) {
            DBObject childObject = (DBObject) child;
            WriteResult wResult = dbCollection.update(new BasicDBObject(MONGO_ID_FIELD, childObject.get(MONGO_ID_FIELD)), childObject);
            return wResult;
        } else {
            throw new RuntimeException("ERROR"); // TODO: fix this
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
                sink.complete();
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
            sink.complete();
        } catch (Exception e) {
            logger().error("", e);  //TODO: properly handle errors
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = (BasicDBObject) createObject(state);
            Object key = basicDBObject.get( MONGO_ID_FIELD );
            if (key != null) {
                if (getDBCollection().findOne (new BasicDBObject( MONGO_ID_FIELD, key )) != null) {
                    responder.resourceAlreadyExists( key.toString() );
                    return;
                }
            }
            WriteResult wResult = getDBCollection().insert(basicDBObject);
        } catch (Exception e) {
            logger().error("", e);
        }

        DBObject newDBObject = getDBCollection().findOne( new BasicDBObject( MONGO_ID_FIELD, basicDBObject.get( MONGO_ID_FIELD ) ) );
        responder.resourceCreated(new MongoBaseObjectResource(this, newDBObject));
    }

    public String toString() {
        return "[MongoCollectionResource: id=" + this.id() + "]";
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("type", "collection");
        sink.accept("count", dbCollection.getCount());
        sink.accept( "capped", dbCollection.isCapped());

        DBObject collectionDBObject = getDBCollection().getDB().getCollection( "system" ).getCollection( "namespaces" ).findOne( new BasicDBObject ( "name", this.getDBCollection().getFullName()));

        if (collectionDBObject != null && collectionDBObject.get("options") != null) {
            DBObject collectionOptions = (DBObject) collectionDBObject.get( "options" );
            sink.accept("max", collectionOptions.get( "max" ));
            sink.accept("size", collectionOptions.get("size"));
        }

        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        // if the state properties are not empty [other than the id value] then throw an error since the other properties are not writable
        if ( state.getPropertyNames().isEmpty() || state.getPropertyNames().size() == 1 && state.getPropertyNames().contains( LiveOak.ID ) ) {
            // if the current state id does not match the current id, then rename the collection.
            if ( state.id()!= null && !state.id().equals( this.id() ) ) {
                // if there already exists a collection by this name, then throw an error
                if ( getDBCollection().getDB().collectionExists( state.id() ) ) {
                    responder.resourceAlreadyExists( state.id() );
                    return;
                }

                this.dbCollection = getDBCollection().rename( state.id() );
            }

            responder.resourceUpdated( this );
            return;
        }

        responder.invalidRequest("Only the ID is updatable on this type of resource collection.");

    }

    public DBObject getChild(String id) {
        DBCursor cursor = getDBCollection().find();
        return getDBCollection().findOne(getMongoID(id));
    }

    protected DBCollection getDBCollection() {
        if (dbCollection == null) {
            this.dbCollection = ((RootMongoResource) parent()).db().getCollection(collectionName);
        }

        return this.dbCollection;
   }

}
