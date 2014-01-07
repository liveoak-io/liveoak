/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.PropertySink;
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
    public void readMember(RequestContext ctx, String id, Responder responder) {
        Object object = getDBObject().get(id);

        if (object == null) {
            responder.noSuchResource(id());
            return;
        }

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

        ReturnFields returnFields = ctx.returnFields();

        DBObject dbObject = getDBObject();

        if (dbObject == null) {
            throw new ResourceProcessingException("Could not find object with ID: " + this.id());
        }

        Set<String> keys = dbObject.keySet();
        for (String key : keys) {
            if (!key.equals(MONGO_ID_FIELD) && !key.equals(MBAAS_ID_FIELD)) {
                Object value = getDBObject().get(key);
                if (value instanceof BasicDBObject) {
                    value = new MongoEmbeddedObjectResource(this, (DBObject) value);
                } else if (value instanceof BasicDBList) {
                    value = getResourceCollection(value);
                } else if (value instanceof DBRef) {
                    value = getResource((DBRef) value, returnFields.child(key).isEmpty());
                }
                sink.accept(key, value);
            }
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {

        DBObject dbObject = getDBObject();
        if (dbObject == null) {
            responder.noSuchResource( this.id() );
            return;
        }

        state.getPropertyNames().stream().forEach((name) -> {
            if (!name.equals(MONGO_ID_FIELD) && !name.equals(MBAAS_ID_FIELD)) {
                Object value = state.getProperty(name);
                if (value instanceof ResourceState) {
                    ResourceState valueResourceState = (ResourceState)value;
                    Object dbRefObject = valueResourceState.getProperty( "$dbref" );
                    try {
                        if (dbRefObject != null) {
                            value = getDBRef((String)dbRefObject);
                        }
                        else {
                            value = createObject( (ResourceState) value );
                        }
                    }catch (Exception e)  {
                        responder.invalidRequest( "Could not update Property");
                        logger().error("", e);
                    }
                }
                if (value == null) {
                    dbObject.removeField( name );
                } else {
                    // if the previous value was a DBRef, check to make sure they are not trying to modify the referenced
                    // resource directly through the referencing resource.
                    if (dbObject.get( name ) instanceof DBRef) {
                        if (value instanceof DBObject) {
                            DBObject valueDBObject = (DBObject) value;
                            if (valueDBObject.get( MONGO_ID_FIELD ) != null)
                            {
                                responder.invalidRequest("Cannot update a DBRef directly");
                                return;
                            }
                        }
                    }
                        dbObject.put(name, value);

                }
            }
        });

        getParent().updateChild(ctx, this.id(), dbObject);

        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        getParent().deleteChild(ctx, id());
        responder.resourceDeleted(this);
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
