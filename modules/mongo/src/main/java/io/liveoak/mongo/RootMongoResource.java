/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.Configurable;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
@Configurable
public class RootMongoResource extends MongoResource implements RootResource {

    private MongoClient mongo;
    protected DB db;
    private String id;
    private MongoConfigResource configResource;

    public RootMongoResource(String id) {
        super(null);
        this.id = id;
        this.configResource = new MongoConfigResource( this );
    }

    protected DB getDB() {
        return this.db;
    }

    protected void configure(MongoClient mongo, DB db) {
        MongoClient oldMongo = this.mongo;
        this.mongo = mongo;
        this.db = db;

        if ( oldMongo != null ) {
            oldMongo.close();
        }
    }

    protected MongoClient mongoClient() {
        return this.mongo;
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Resource configuration() {
        return this.configResource;
    }

    @Override
    public void destroy() {
        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        if (db.collectionExists(id)) {
            responder.resourceRead(new MongoCollectionResource(this, db.getCollection(id)));
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        Pagination pagination = ctx.pagination();
        Stream<String> members = this.db.getCollectionNames().stream().skip(pagination.offset());
        if (pagination.limit() > 0) {
            members = members.limit(pagination.limit());
        }

        members.forEach((name) -> {
            if (!name.equals("system.indexes")) {
                sink.accept(new MongoCollectionResource(this, db.getCollection(name)));
            }
        });


        sink.close();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {

        String id = state.id();

        if (id == null || !db.collectionExists(id)) {

            if (id == null) {
                id = UUID.randomUUID().toString();
            }

            DBCollection collection = db.createCollection(id, new BasicDBObject()); //send an empty DBOBject instead of null, since setting null will not actually create the collection until a write

            responder.resourceCreated(new MongoCollectionResource(this, collection));
        } else {
            responder.resourceAlreadyExists(id);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("type", "collection");
        sink.close();
    }

}
