/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
import io.liveoak.spi.resource.async.Resource;
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
    public Resource member(RequestContext ctx, String childId) {

        if ("_aggregate".equals(childId)) {
            return new MongoAggregationResource(this);
        }

        DBObject object = dbCollection.findOne(getMongoIDDBOBject(childId));

        if (object != null) {
            return new MongoBaseObjectResource(this, object);
        }
        return null;
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
        wResult = getDBCollection().remove(getMongoIDDBOBject(childId));
        return wResult;
    }

    protected Object updateChild(RequestContext ctx, String childId, Object child) {
        if (child instanceof DBObject) {
            DBObject childObject = (DBObject) child;
            WriteResult wResult = dbCollection.update(getMongoIDDBOBject(childId), childObject);
            return wResult;
        } else {
            throw new RuntimeException("ERROR"); // TODO: fix this
        }
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {

        LinkedList<Resource> members = new LinkedList<>();
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
                members.add(new MongoEmbeddedObjectResource(this, dbCursor.explain()));
                return members;
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
            members.add(new MongoBaseObjectResource(this, dbObject));
        });

        return members;
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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "collection");
        result.put("count", dbCollection.getCount());
        result.put("capped", dbCollection.isCapped());

        DBObject collectionDBObject = getDBCollection().getDB().getCollection( "system" ).getCollection( "namespaces" ).findOne( new BasicDBObject ( "name", this.getDBCollection().getFullName()));

        if (collectionDBObject != null && collectionDBObject.get("options") != null) {
            DBObject collectionOptions = (DBObject) collectionDBObject.get( "options" );
            result.put("max", collectionOptions.get( "max" ));
            result.put("size", collectionOptions.get("size"));
        }

        return result;
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
        return getDBCollection().findOne(getMongoIDDBOBject(id));
    }

    protected DBCollection getDBCollection() {
        if (dbCollection == null) {
            this.dbCollection = ((RootMongoResource) parent()).db().getCollection(collectionName);
        }

        return this.dbCollection;
   }

}
