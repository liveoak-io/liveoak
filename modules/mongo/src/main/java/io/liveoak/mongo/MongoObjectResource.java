package io.liveoak.mongo;

import com.mongodb.*;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class MongoObjectResource extends MongoResource {

    public MongoObjectResource(MongoResource parent, DBObject dbObject) {
        super(parent, dbObject);
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        if (this.dbObject instanceof BasicDBObject) {
            Object candidateId = this.dbObject.get(MONGO_ID_FIELD);
            if (candidateId == null) {
                return this.parent().id();
            }
            return candidateId.toString();
        } else if (this.dbObject instanceof BasicDBList) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        Set<String> keys = this.dbObject.keySet();
        for (String key : keys) {
            if (!key.equals(MONGO_ID_FIELD) && !key.equals(MBAAS_ID_FIELD)) {
                Object value = this.dbObject.get(key);
                if (value instanceof BasicDBObject) {
                    value = new MongoObjectResource(this, (DBObject) value);
                }
                sink.accept(key, value);
            }
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {
        state.getPropertyNames().stream().forEach((name) -> {
            if ( ! name.equals( MONGO_ID_FIELD ) && ! name.equals(MBAAS_ID_FIELD ) ) {
                this.dbObject.put( name, state.getProperty( name ) );
            }
        } );

        this.parent.getDB().getCollection(parent.id()).update(new BasicDBObject().append(MONGO_ID_FIELD, this.dbObject.get(MONGO_ID_FIELD)), this.dbObject);

        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        DB db = parent.getDB();
        if (db.collectionExists(parent.id())) {
            DBCollection dbCollection = db.getCollection(parent.id());
            DBObject dbObject = dbCollection.findOne(this.dbObject);
            if (dbObject != null) {
                dbCollection.remove(dbObject);
                responder.resourceDeleted(this);
            }
        }

        responder.noSuchResource(id());
    }

    public String toString() {
        return "[MongoObject: obj=" + this.dbObject() + "]";
    }
}
