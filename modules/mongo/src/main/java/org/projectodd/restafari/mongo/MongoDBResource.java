package org.projectodd.restafari.mongo;

import com.mongodb.*;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ResourceState;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MongoDBResource implements CollectionResource, RootResource {

    private String id;
    private MongoClient mongo;
    private DB db;

    public MongoDBResource(String id) {
        this.id = id;
    }

    @Override
    public Resource parent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String id() {
        return this.id;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        Config config = context.config();
        String host = config.get("host", "localhost");
        int port = config.get("port", 27017);
        String dbName = config.getRequired("db");

        try {
            mongo = new MongoClient(host, port);
            db = mongo.getDB(dbName);
            if (db == null) {
                throw new InitializationException("Unknown database " + dbName);
            }
        } catch (UnknownHostException e) {
            throw new InitializationException("Could not handleConnect to " + host + " on port " + port);
        }
    }

    DB getDB() {
        return this.db;
    }

    @Override
    public void destroy() {
        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void read(String id, Responder responder) {
        if (db.collectionExists(id)) {
            responder.resourceRead(new MongoCollectionResource(this, id));
        } else {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        if (pagination.getLimit() > 0 || pagination.getOffset() > 0) {
            Stream<? extends Resource> members = this.db.getCollectionNames().stream().substream(pagination.getOffset()).limit(pagination.getLimit()).map((name) -> {
                return new MongoCollectionResource( this, name );
            });
            responder.resourceRead(new SimplePaginatedCollectionResource<CollectionResource>(this, pagination, members) );
        } else {
            responder.resourceRead(this);
        }
    }

    @Override
    public void create(ResourceState state, Responder responder) {

        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        db.createCollection(id, null);
        responder.resourceCreated(new MongoCollectionResource(this, id));
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        this.db.getCollectionNames().forEach((name) -> {
            sink.accept( new MongoCollectionResource(this, name) );
        });

        sink.close();
    }

}
