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
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
class MongoCollectionResource extends MongoResource {

    DBCollection dbCollection;

    MongoCollectionResource( RootMongoResource parent, DBCollection collection ) {
        super( parent );
        this.dbCollection = collection;
    }

    @Override
    public void readMember( RequestContext ctx, String childId, Responder responder ) {
        DBObject object = null;
        //TODO: figure out this whole object id thing better
        if ( ObjectId.isValid( childId ) ) {
            object = dbCollection.findOne( new BasicDBObject( MONGO_ID_FIELD, new ObjectId( childId ) ) );
        }

        if ( object == null ) {
            object = dbCollection.findOne( new BasicDBObject( MONGO_ID_FIELD, childId ) );
        }

        if ( object == null ) {
            responder.noSuchResource( childId );
        } else {
            responder.resourceRead( new MongoObjectResource( this, object, null ) );
        }
    }

    @Override
    public String id() {
        return dbCollection.getName();
    }

    @Override
    public void delete( RequestContext ctx, Responder responder ) {
        dbCollection.drop();
        responder.resourceDeleted( this );
    }

    @Override
    protected WriteResult deleteChild( RequestContext ctx, String childId ) {
        WriteResult wResult = null;
        if ( ObjectId.isValid( childId ) ) {
            wResult = dbCollection.remove( new BasicDBObject( MONGO_ID_FIELD, new ObjectId( childId ) ) );
        }
        if ( wResult == null || wResult.getN() == 0 ) {
            wResult = dbCollection.remove( new BasicDBObject( MONGO_ID_FIELD, childId ) );
        }

        return wResult;
    }

    @Override
    protected Object updateChild( RequestContext ctx, String childId, Object child ) {
        if ( child instanceof DBObject ) {
            DBObject childObject = ( DBObject ) child;
            WriteResult wResult = dbCollection.update( new BasicDBObject( MONGO_ID_FIELD, childObject.get( MONGO_ID_FIELD ) ), childObject );
            return wResult;
        } else {
            throw new RuntimeException( "ERROR" ); //TODO: fix this
        }
    }

    @Override
    public void readMembers( RequestContext ctx, ResourceSink sink ) {
        DBObject queryObject = new BasicDBObject();
        if ( ctx != null && ctx.getResourceParams() != null && ctx.getResourceParams().contains( "q" ) ) {
            String queryString = ctx.getResourceParams().value( "q" );
            queryObject = ( DBObject ) JSON.parse( queryString );
        }

        DBObject returnFields = new BasicDBObject();
        if ( ctx != null && ctx.getReturnFields() != null && !ctx.getReturnFields().isAll() ) {
            ctx.getReturnFields().forEach( ( fieldName ) -> {
                returnFields.put( fieldName, true );
            } );
        }

        DBCursor dbCursor = dbCollection.find( queryObject, returnFields );

        if ( ctx != null && ctx.getPagination() != null ) {
            dbCursor.limit( ctx.getPagination().limit() );
            dbCursor.skip( ctx.getPagination().offset() );
        }

        dbCursor.forEach( ( dbObject ) -> {
            sink.accept( new MongoObjectResource( this, dbObject, null ) );
        } );

        try {
            sink.close();
        } catch ( Exception e ) {
            e.printStackTrace();  //TODO: properly handle errors
        }
    }

    @Override
    public void createMember( RequestContext ctx, ResourceState state, Responder responder ) {
        BasicDBObject basicDBObject = null;
        try {
            basicDBObject = ( BasicDBObject ) createObject( state );
            WriteResult wResult = dbCollection.insert( basicDBObject );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        responder.resourceCreated( new MongoObjectResource( this, basicDBObject, null ) );
    }

    public String toString() {
        return "[MongoCollectionResource: id=" + this.id() + "]";
    }

    protected Object createObject( ResourceState resourceState ) {
        BasicDBObject basicDBObject = new BasicDBObject();
        // if the state already has an id set, use it here. Otherwise one will be autocreated on insert
        String rid = resourceState.id();
        if ( rid != null ) {
            basicDBObject.append( MONGO_ID_FIELD, rid );
        }

        Set<String> keys = resourceState.getPropertyNames();

        for ( String key : keys ) {
            if ( !key.equals( MBAAS_ID_FIELD ) ) { //don't append the ID field again
                Object value = resourceState.getProperty( key );
                if ( value instanceof ResourceState ) {
                    value = createObject( ( ResourceState ) value );
                }
                basicDBObject.append( key, value );
            }
        }

        return basicDBObject;
    }

    @Override
    public void readProperties( RequestContext ctx, PropertySink sink ) {
        sink.accept( "type", "collection" );
        sink.close();
    }

}
