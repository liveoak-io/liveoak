/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.UUID;
import java.util.stream.Stream;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoClient;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceProcessingException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigMapping;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
@Configurable
public class RootMongoResource extends MongoResource implements RootResource {

    private MongoClient mongo;
    protected DB db;
    private String id;

    public RootMongoResource(String id) {
        super(null);
        this.id = id;
    }

    @ConfigMapping({@ConfigProperty("host"), @ConfigProperty("port"), @ConfigProperty("db")})
    private void updateConfig(Object... values) throws Exception {

        String host;
        Object hostObject = values[0];
        if (hostObject == null) {
            host = "localhost";
        } else if (!(hostObject instanceof String) || ((String)(hostObject)).isEmpty()) {
            throw new InitializationException("Configuration value for 'host' invalid. Requires a string value. Received : " + hostObject);
        } else {
            host = (String) hostObject;
        }

        Integer port;
        Object portObject = values[1];
        if (portObject == null) {
            port = 27017;
        } else if (!(portObject instanceof Integer)) {
            throw new InitializationException("Configuration value for 'port' invalid. Requires an integer value. Received : " + portObject);
        } else {
            port = (Integer) portObject;
        }

        String dbName = (String)values[2];
        if (dbName == null || dbName.isEmpty()) {
            throw new InitializationException("String configuration value required for 'db'");
        }

        MongoClient mongo = new MongoClient(host, port);
        DB db = mongo.getDB(dbName);
        if (db == null) {
            throw new InitializationException("Unknown database " + dbName);
        }

        this.configure(mongo, db);
    }

    @ConfigMappingExporter("host")
    public Object configHost() {
        return client().getAddress().getHost();
    }

    @ConfigMappingExporter("port")
    public Object configPort() {
        return client().getAddress().getPort();
    }

    @ConfigMappingExporter("db")
    public Object configDb() {
        return db().getName();
    }

    protected void configure(MongoClient mongo, DB db) {
        MongoClient oldMongo = this.mongo;
        this.mongo = mongo;
        this.db = db;

        if (oldMongo != null) {
            oldMongo.close();
        }
    }

    MongoClient client() {
        return this.mongo;
    }

    DB db() {
        return this.db;
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

            DBCollection collection = db.createCollection(id, new BasicDBObject()); // send an empty DBOBject instead of null, since setting null will not actually create the collection until a write

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

    @Override
    protected MongoObjectResource getResource(DBRef dbRef, boolean byReference) throws ResourceProcessingException {
        if (dbRef != null)
        {
            if (dbRef.getDB() == null) {
                throw new ResourceProcessingException("Invalid Reference. Reference Database is null.");
            }
            if (dbRef.getDB().getName() != db.getName()) {
                throw new ResourceProcessingException("LiveOak only supports MongoResource objects within the same database.");
            }

            String collectionName = dbRef.getRef();
            if (!db.collectionExists(collectionName)) {
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
                    return new DBRef(this.db, collectionName, resourceID);
                }

            } else {
                throw new ResourceProcessingException("$DBRefs are only supported under the same context root. URL specified ('" + uri + "')should start with " + rootURI
                        + ".");
            }
        }
        else {
            throw new ResourceProcessingException("$DBRefs must be URL, they cannot be null.");
        }
    }
}
