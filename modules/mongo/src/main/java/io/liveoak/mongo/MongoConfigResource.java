package io.liveoak.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.ConfigResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class MongoConfigResource implements ConfigResource {

    protected MongoClient mongoClient;
    protected DB database;
    private RootMongoResource parent;

    public MongoConfigResource(RootMongoResource parent, MongoClient mongoClient, DB database) {
        this.parent = parent;
        this.mongoClient = mongoClient;
        this.database = database;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

        Object hostObject = state.getProperty("host");
        if (!(hostObject instanceof String) || ((String)(hostObject)).isEmpty()) {
            throw new InitializationException("Configuration value for 'host' invalid. Requires a string value. Received : " + hostObject);
        }

        String host = (String) hostObject;
        if (host == null) {
            host = "localhost";
        }

        Object portObject = state.getProperty("port");
        if (portObject == null || !(portObject instanceof Integer)) {
            throw new InitializationException("Configuration value for 'port' invalid. Requires an integer value. Received : " + portObject);
        }

        Integer port = (Integer) state.getProperty("port");
        if (port == null) {
            port = 27017;
        }

        String dbName = (String) state.getProperty("db");

        if (dbName == null || dbName.isEmpty()) {
            throw new InitializationException("Configuration value required for 'db'");
        }

        MongoClient mongo = new MongoClient(host, port);
        DB db = mongo.getDB(dbName);
        if (db == null) {
            throw new InitializationException("Unknown database " + dbName);
        }

        this.parent.configure(mongo, db);
        this.mongoClient = mongo;
        this.database = db;
        responder.resourceUpdated(this);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("host", mongoClient.getAddress().getHost());
        sink.accept("port", mongoClient.getAddress().getPort());
        sink.accept("db", database.getName());
        sink.close();
    }
}
