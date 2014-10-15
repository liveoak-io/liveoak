package io.liveoak.mongo.config;

import com.mongodb.DB;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RootMongoConfigResource implements ConfigResource, RootResource {

    Resource parent;
    String id;

    MongoConfig mongoConfig;
    MongoDatastoresRegistry mongoDatastoresResource;

    public RootMongoConfigResource(String id, MongoDatastoresRegistry mongoSystemConfigResource) {
        this.id = id;
        this.mongoDatastoresResource = mongoSystemConfigResource;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        try {
            this.mongoConfig = new MongoConfig(this, mongoDatastoresResource, state);
            responder.resourceUpdated(this);
        } catch (Exception e) {
            throw new InitializationException("Error trying to initilize the LiveOak Mongo Module",e);
        }
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.mongoConfig = new MongoConfig(this, mongoDatastoresResource, state);
        responder.resourceUpdated(this);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        for (String property: mongoConfig.properties(ctx).keySet()) {
            sink.accept(property, mongoConfig.properties(ctx).get(property));
        }
        sink.complete();
    }

    public DB getDB() {
        return mongoConfig.getDB();
    }

    public void close() {
        mongoConfig.close();
    }

}
