/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import io.liveoak.common.util.PagingLinksBuilder;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class MongoCollectionResource extends MongoResource {

    private DBCollection dbCollection;
    private String collectionName;

    private boolean explainQuery;
    private DBObject queryObject;

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {

        queryObject = new BasicDBObject();
        ResourceParams resourceParams = ctx.resourceParams();
        if (resourceParams != null) {
            if (resourceParams.contains("q")) {
                String queryString = resourceParams.value("q");
                try {
                    queryObject = (DBObject) JSON.parse(queryString);
                } catch (Exception e) {
                    throw new NotAcceptableException(uri().toString(), "Invalid JSON format for the 'query' parameter", e);
                }
            }
        }

        explainQuery = resourceParams != null
                && resourceParams.contains("explain")
                && resourceParams.value("explain").equalsIgnoreCase("true");

        DBObject returnFields = new BasicDBObject();
        if (ctx.returnFields() != null && !ctx.returnFields().isAll()) {
            ctx.returnFields().forEach((fieldName) -> {
                returnFields.put(fieldName, true);
            });
        }

        int totalCount = explainQuery ? 1 : (int) dbCollection.getCount(queryObject, returnFields);
        int count = ctx.pagination().offset() >= totalCount ? 0 : totalCount - ctx.pagination().offset();
        count = count < ctx.pagination().limit() ? count : ctx.pagination().limit();

        List<Resource> links = new LinkedList<>();

        PagingLinksBuilder linksBuilder = new PagingLinksBuilder(ctx)
                .uri(uri())
                .count(count)
                .totalCount(totalCount);

        links.addAll(linksBuilder.build());

        Map<String, Object> result = new HashMap<>();
        if (links.size() > 0) {
            result.put("links", links);
        }
        result.put("type", "collection");
        result.put("count", (long) totalCount);
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
    public Collection<Resource> members(RequestContext ctx) throws Exception {

        LinkedList<Resource> members = new LinkedList<>();

        DBObject returnFields = new BasicDBObject();
        if (ctx.returnFields() != null && !ctx.returnFields().child(LiveOak.MEMBERS).isEmpty()) {
            ReturnFields membersReturnFields = ctx.returnFields().child(LiveOak.MEMBERS);
            if (!membersReturnFields.isAll()) {
                membersReturnFields.forEach((fieldName) -> {
                    returnFields.put(fieldName, true);
                });
            }
        }

        DBCursor dbCursor = dbCollection.find(queryObject, returnFields);

        ResourceParams resourceParams = ctx.resourceParams();
        if (resourceParams != null && resourceParams.contains("hint")) {
            String hint = resourceParams.value("hint");
            if (hint.startsWith("{")) {
                try {
                    DBObject hintObject = (DBObject) JSON.parse(hint);
                    dbCursor.hint(hintObject) ;
                } catch (Exception e) {
                    throw new NotAcceptableException(uri().toString(), "Invalid JSON format for the 'hint' parameter", e);
                }
            } else {
                dbCursor.hint(hint);
            }
        }

        if (explainQuery) {
            members.add(new MongoEmbeddedObjectResource(this, dbCursor.explain()));
        } else {
            Sorting sorting = ctx.sorting();
            if (sorting != null) {
                BasicDBObject sortingObject = new BasicDBObject();
                for (Sorting.Spec spec : sorting) {
                    sortingObject.append(spec.name(), spec.ascending() ? 1 : -1);
                }
                dbCursor = dbCursor.sort(sortingObject);
            }

            Pagination pagination = ctx.pagination();
            if (pagination != null) {
                dbCursor.limit(pagination.limit());
                dbCursor.skip(pagination.offset());
            }

            try {
                dbCursor.hasNext();
            } catch (Exception e) {
                throw new ResourceProcessingException("Exception encountered trying to fetch data from the Mongo Database", e);
            }

            dbCursor.forEach((dbObject) -> {
                members.add(new MongoBaseObjectResource(this, dbObject));
            });
        }

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
