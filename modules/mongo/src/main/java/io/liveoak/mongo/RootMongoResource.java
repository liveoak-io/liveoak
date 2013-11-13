package io.liveoak.mongo;

import com.mongodb.*;
import io.liveoak.spi.*;
import io.liveoak.spi.resource.async.*;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RootMongoResource extends MongoResource implements RootResource {

    private MongoClient mongo;
    private DB db;

    public RootMongoResource() {
        super(null, (String) null);
    }

    public RootMongoResource(String id) {
        super(null, id);
    }

    @Override
    public Resource parent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {

        if (this.id == null) {
            this.id = context.config().get("id", null);
            if (this.id == null) {
                throw new InitializationException("no id specified");
            }
        }

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

    protected DB getDB() {
        return this.db;
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
            responder.resourceRead(new MongoCollectionResource(this, id));
        } else {
            responder.noSuchResource(id);
        }

    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        Pagination pagination = ctx.getPagination();
        Stream<String> members = this.db.getCollectionNames().stream().skip(pagination.offset());
        if (pagination.limit() > 0) {
            members = members.limit(pagination.limit());
        }

        members.forEach((name) -> {
            if (!name.equals("system.indexes")) {
                sink.accept(new MongoCollectionResource(this, name));
            }
        });

        sink.close();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {

        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        db.createCollection(id, new BasicDBObject()); //send an empty DBOBject instead of null, since setting null will not actually create the collection until a write
        responder.resourceCreated(new MongoCollectionResource(this, id));
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        sink.accept( "type", "collection" );
        sink.close();
    }
}
