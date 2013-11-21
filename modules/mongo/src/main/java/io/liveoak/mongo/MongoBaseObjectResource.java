/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.DBObject;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoBaseObjectResource extends MongoObjectResource {

    public MongoBaseObjectResource( MongoCollectionResource parent, DBObject dbObject ) {
        super( parent, dbObject );
    }

    @Override
    public void updateProperties( RequestContext ctx, ResourceState state, Responder responder ) {
        state.getPropertyNames().stream().forEach( ( name ) -> {
            if ( !name.equals( MONGO_ID_FIELD ) && !name.equals( MBAAS_ID_FIELD ) ) {
                this.dbObject.put( name, state.getProperty( name ) );
            }
        } );

        getParent().updateChild( ctx, this.id(), this.dbObject );

        responder.resourceUpdated( this );
    }

    @Override
    public void delete( RequestContext ctx, Responder responder ) {
        getParent().deleteChild( ctx, id() );
        responder.resourceDeleted( this );
    }

    protected MongoCollectionResource getParent() {
        return (MongoCollectionResource) parent;
    }
}
