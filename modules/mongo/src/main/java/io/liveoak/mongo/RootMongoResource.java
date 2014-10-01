/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RootMongoResource extends MongoResource implements RootResource {

    private Resource parent;
    private RootMongoConfigResource mongoConfigResource;
    private String id;

    public RootMongoResource(String id) {
        super(null);
        this.id = id;
        mongoConfigResource = new RootMongoConfigResource(id);
    }

    public RootMongoConfigResource configuration() {
        return mongoConfigResource;
    }

    MongoClient client() {
        return this.mongoConfigResource.getMongoClient();
    }

    DB db() {
        return this.mongoConfigResource.getDB();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void destroy() {
        if (client() != null) {
            client().close();
        }
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        if (db().collectionExists(id)) {
            return new MongoCollectionResource(this, db().getCollection(id));
        }
        return null;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) {
        Pagination pagination = ctx.pagination();
        Stream<String> members = this.db().getCollectionNames().stream().skip(pagination.offset());
        if (pagination.limit() > 0) {
            members = members.limit(pagination.limit());
        }

        return members
                .filter(name -> !name.equals("system.indexes"))
                .map(name -> new MongoCollectionResource(this, db().getCollection(name)))
                .collect(Collectors.toList());
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {

        String id = state.id();

        if (id == null || !db().collectionExists(id)) {

            if (id == null) {
                id = UUID.randomUUID().toString();
            }

            BasicDBObject options = new BasicDBObject();

            Object capped = state.getProperty("capped");
            if (capped != null) {
                options.put("capped", capped);
            }

            Object autoIndexId = state.getProperty("autoIndexId");
            if (autoIndexId != null) {
                options.put("autoIndexId", autoIndexId);
            }

            Object size = state.getProperty("size");
            if (size != null) {
                options.put("size", size);
            }

            Object max = state.getProperty("max");
            if (max != null) {
                options.put("max", max);
            }

            DBCollection collection = db().createCollection(id, options); // send an empty DBOBject instead of null, since setting null will not actually create the collection until a write

            responder.resourceCreated(new MongoCollectionResource(this, collection));
        } else {
            responder.resourceAlreadyExists(id);
        }
    }

    @Override
    public ResourceState properties() {
        ResourceState result = new DefaultResourceState();
        result.putProperty("type", "database");
        int count = this.db().getCollectionNames().size();
        if (count >= 1) {
            count = count - 1; // -1 due to not showing internal 'system.index' collection, which exists if another collection exists
        }
        result.putProperty("count", count);
        return result;
    }

    @Override
    protected MongoObjectResource getResource(DBRef dbRef, boolean byReference) throws ResourceProcessingException {
        if (dbRef != null) {
            if (dbRef.getDB() == null) {
                throw new ResourceProcessingException("Invalid Reference. Reference Database is null.");
            }
            if (dbRef.getDB().getName() != db().getName()) {
                throw new ResourceProcessingException("LiveOak only supports MongoResource objects within the same database.");
            }

            String collectionName = dbRef.getRef();
            if (!db().collectionExists(collectionName)) {
                throw new ResourceProcessingException("Cannot find collection specified in a reference. No collection named '" + collectionName + "' found");
            }

            MongoCollectionResource mongoCollectionResource = new MongoCollectionResource(this, collectionName);

            if (byReference) {

                MongoObjectResource mongoObjectResource = new MongoBaseObjectResource(mongoCollectionResource, dbRef.getId());

                return mongoObjectResource;
            } else {
                DBObject referencedObject = dbRef.fetch();
                if (referencedObject == null) {
                    throw new ResourceProcessingException("Cannot find referenced resource. No resource in collection '" + collectionName + "' with id '" + dbRef.getId()
                            + "'");
                }
                return new MongoBaseObjectResource(mongoCollectionResource, referencedObject);
            }
        } else {
            throw new ResourceProcessingException("Invalid Reference. Reference cannot be null.");
        }
    }

    @Override
    protected DBRef getDBRef(String uri) throws ResourceProcessingException {
        if (uri != null) {
            String rootURI = this.uri().toString();
            if (uri.startsWith(rootURI + "/")) {

                String resourcePath = uri.substring(rootURI.length());
                String[] paths = resourcePath.split("/");

                if (paths.length != 3) {
                    throw new ResourceProcessingException("$DBRefs must be in the format /rootContextPath/collectionName/resourceID");
                } else {
                    String collectionName = paths[1];
                    String resourceID = paths[2];
                    return new DBRef(this.db(), collectionName, resourceID);
                }

            } else {
                throw new ResourceProcessingException("$DBRefs are only supported under the same context root. URL specified ('" + uri + "')should start with " + rootURI
                        + ".");
            }
        } else {
            throw new ResourceProcessingException("$DBRefs must be URL, they cannot be null.");
        }
    }

}
