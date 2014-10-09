/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoBaseObjectResource extends MongoObjectResource {

    Object id;

    public MongoBaseObjectResource(MongoCollectionResource parent, DBObject dbObject) {
        super(parent, dbObject);
    }

    public MongoBaseObjectResource(MongoCollectionResource parent, Object id) {
        super(parent, null);
        this.id = id;
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        Object object = getDBObject().get(id);

        if (object == null) {
            return null;
        }

        if (object != null) {
            if (object instanceof BasicDBObject || object instanceof BasicDBList) {
                return null;
            } else if (object instanceof DBRef) {
                // TODO In case of exception make sure it results in InvalidRequest
                //try {
                    return getResource((DBRef) object, ctx.returnFields().child(id).isEmpty());
                //} catch (ResourceProcessingException e) {
                //    responder.invalidRequest(e.getMessage());
                //}
            } else {
                throw new RuntimeException("ERROR: Object type (" + object.getClass() + ") not recognized");
            }
        }
        return null;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        // TODO: only read properties specified in the return fields and not everything
        Map<String, Object> result = new HashMap<>();
        ReturnFields returnFields = ctx.returnFields();

        DBObject dbObject = getDBObject();

        if (dbObject == null) {
            throw new ResourceProcessingException("Could not find object with ID: " + this.id());
        }

        Set<String> keys = dbObject.keySet();
        for (String key : keys) {
            if (!key.equals(MONGO_ID_FIELD) && !key.equals(LiveOak.ID)) {
                Object value = getDBObject().get(key);
                if (value instanceof BasicDBObject) {
                    value = new MongoEmbeddedObjectResource(this, (DBObject) value);
                } else if (value instanceof BasicDBList) {
                    value = getResourceCollection(value);
                } else if (value instanceof DBRef) {
                    value = getResource((DBRef) value, returnFields.child(key).isEmpty());
                }

                if (supportedObject(value)) {
                    result.put(key, value);
                } else {
                    log.warn("Unsupported Property type " + value.getClass() + " cannot encode.");
                }
            }
        }
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {

        DBObject dbObject = getDBObject();
        if (dbObject == null) {
            responder.noSuchResource( this.id() );
            return;
        }

        try {

            state.getPropertyNames().stream().forEach((name) -> {
                //if the previous value was a DBRef, check to make sure they are not trying to modify the referenced
                //resource directly through the referencing resource.
                if (dbObject.get( name ) instanceof DBRef) {
                    Object value = state.getProperty(name);
                    if (value instanceof ResourceState) {
                        ResourceState valueResourceState = (ResourceState) value;
                        if (valueResourceState.id() != null)
                        {
                            responder.invalidRequest("Cannot update a DBRef directly");
                            throw new RuntimeException("Invalid request: Cannot update a DBRef directly!");
                        }
                    }
                }
            });

            DBObject basicDBObject = (BasicDBObject) createObject(state);
            if (getResourceID(basicDBObject) != null && !getResourceID(basicDBObject).equals(this.id())) {
                responder.invalidRequest("Modifying an id is not allowed on an update.");
            }

            basicDBObject.put(MONGO_ID_FIELD, getMongoID(this.id()));

            this.dbObject = basicDBObject;
            getParent().updateChild(ctx, this.id(), basicDBObject);

            responder.resourceUpdated(this);
        } catch (Exception e) {
            responder.internalError("An error occured while updating the mongo resource. Resource Not updated.", e);
        }
    }

    private Object convertCollection(Responder responder, Collection<?> value) {
        List<Object> newCol = new ArrayList<Object>();
        for(Object tmp: value) {
            if (tmp instanceof ResourceState) {
                tmp = convertResourceState(responder, (ResourceState)tmp);
            }
            newCol.add(tmp);
        }
        return newCol;
    }

    private Object convertResourceState(Responder responder, ResourceState valueResourceState) {
        Object tmp = null;
        Object dbRefObject = valueResourceState.getProperty( "$dbref" );
        try {
            if (dbRefObject != null) {
                tmp = getDBRef((String)dbRefObject);
            } else {
                tmp = createObject( (ResourceState) valueResourceState );
            }
        } catch (Exception e) {
            responder.invalidRequest( "Could not update Property");
            throw new RuntimeException("Could not update property!", e);
        }
        return tmp;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        if (getParent().getDBCollection().isCapped()) {
            responder.deleteNotSupported(this);
        } else {
            getParent().deleteChild(ctx, id());
            responder.resourceDeleted(this);

        }
    }

    protected MongoCollectionResource getParent() {
        return (MongoCollectionResource) parent();
    }

    @Override
    public String id() {
        if (this.id != null) {
            return getResourceID(this.id);
        } else {
            return super.id();
        }
    }

    @Override
    protected DBObject getDBObject() {
        if (dbObject == null) {
            dbObject = getParent().getChild(getResourceID(this.id));
        }

        return dbObject;
    }
}
