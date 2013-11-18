/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class MongoObjectResource extends MongoResource {

    private DBObject dbObject;
    private String id;

    public MongoObjectResource( MongoResource parent, DBObject dbObject, String id ) {
        super( parent );
        this.dbObject = dbObject;
        this.id = id;
    }

    @Override
    public void readMember( RequestContext ctx, String id, Responder responder ) {
        Object object = this.dbObject.get( id );
        if ( object != null ) {
            if ( object instanceof BasicDBObject ) {
                responder.resourceRead( new MongoObjectResource( this, ( DBObject ) object, id ) );
            } else {
                responder.internalError( "ERROR: Object type (" + object.getClass() + ") not recognized" );
            }
        } else {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void readProperties( RequestContext ctx, PropertySink sink ) {
        Set<String> keys = this.dbObject.keySet();
        for ( String key : keys ) {
            if ( !key.equals( MONGO_ID_FIELD ) && !key.equals( MBAAS_ID_FIELD ) ) {
                Object value = this.dbObject.get( key );
                if ( value instanceof BasicDBObject ) {
                    value = new MongoObjectResource( this, ( DBObject ) value, key );
                }
                sink.accept( key, value );
            }
        }
        sink.close();
    }

    @Override
    public void updateProperties( RequestContext ctx, ResourceState state, Responder responder ) {
        state.getPropertyNames().stream().forEach( ( name ) -> {
            if ( !name.equals( MONGO_ID_FIELD ) && !name.equals( MBAAS_ID_FIELD ) ) {
                this.dbObject.put( name, state.getProperty( name ) );
            }
        } );

        this.parent.updateChild( ctx, this.id(), this.dbObject );

        responder.resourceUpdated( this );
    }

    @Override
    public void delete( RequestContext ctx, Responder responder ) {
        parent.deleteChild( ctx, id() );
        responder.resourceDeleted( this );
    }

    @Override
    protected Object updateChild( RequestContext ctx, String childId, Object child ) {
        this.dbObject.put( childId, child );
        return parent.updateChild( ctx, this.id(), this.dbObject );
    }

    @Override
    protected Object deleteChild( RequestContext ctx, String childId ) {
        dbObject.removeField( childId );
        return parent.updateChild( ctx, this.id(), dbObject );
    }

    public String toString() {
        return "[MongoObject: obj=" + this.dbObject + "]";
    }

    @Override
    public String id() {
        if ( id != null ) {
            return id;
        } else {
            return this.dbObject.get( MONGO_ID_FIELD ).toString();
        }
    }
}
